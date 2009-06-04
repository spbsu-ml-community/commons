package com.spbsu.util.persist;

import com.spbsu.util.Computable;
import com.spbsu.util.Converter;
import com.spbsu.util.cache.CacheStrategy;
import com.spbsu.util.cache.FixedSizeCache;
import com.spbsu.util.persist.pages.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 08.04.2008
 * Time: 15:44:15
 * To change this template use File | Settings | File Templates.
 */
public class BTreeMap<K, V> implements Map<K, V> {
  private final FileChannel file;
  private final FixedSizeCache<Integer, BTreePage> pageCache;
  public static final int SLOT_CAPACITY = 32 * 1024;
  public final PageManager pageManager;
  public final Converter<K, ByteBuffer> keyConverter;
  final Converter<V, ByteBuffer> valueConverter;
  final FixedSizeCache<K, V> valuesCache;

  public BTreeMap(File storage, Converter<K, ByteBuffer> keyConverter, Converter<V, ByteBuffer> valueConverter, int cacheSize) throws IOException {
    this(storage, keyConverter, valueConverter, cacheSize, 1000);
  }

  public BTreeMap(File storage, Converter<K, ByteBuffer> keyConverter, Converter<V, ByteBuffer> valueConverter, int cacheSize, int valuesCacheSize) throws IOException {
    valuesCache = new FixedSizeCache<K,V>(valuesCacheSize, CacheStrategy.Type.LRU);
    this.keyConverter = keyConverter;
    this.valueConverter = valueConverter;
    file = new RandomAccessFile(storage, "rw").getChannel();
    pageCache = new FixedSizeCache<Integer, BTreePage>(cacheSize, CacheStrategy.Type.LRU);
    if(fileSize() == 0) pageManager = new PageManager(this);
    else pageManager = (PageManager)getPage(0);
  }

  public void close() throws IOException {
    pageManager.write();
    pageCache.clear();
    file.close();
  }

  public void flush(){
    pageManager.write();
    pageCache.clear();
  }

  public int size() {
    return pageManager.size();
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public BTreePage getPage(long id){
    int intid = (int) (id & 0xFFFFFF);
    final long pageOffset = (long)intid * SLOT_CAPACITY;
    try {
      if (fileSize() < pageOffset + SLOT_CAPACITY) return null;
      return pageCache.get(intid, new Computable<Integer, BTreePage>() {
        public BTreePage compute(Integer argument) {
          try {
            return BTreePage.read(BTreeMap.this, argument);
          }
          catch (IOException e) {
            throw new RuntimeException("IO exception during BTree page read: ", e);
          }
        }
      });
    }
    catch (IOException e) {      
      throw new RuntimeException(e);
    }
  }

  public synchronized <X extends BTreePage> X createPage(final X emptyPage){
    try {
      //noinspection unchecked
      return (X)pageCache.get((int)(fileSize() / SLOT_CAPACITY), new Computable<Integer, BTreePage>() {
        public BTreePage compute(Integer argument) {
          try {
            final byte[] tmpBuff = new byte[SLOT_CAPACITY];
            Arrays.fill(tmpBuff, (byte) 0);
            file.position(fileSize()).write(ByteBuffer.wrap(tmpBuff));
            size += SLOT_CAPACITY;
            emptyPage.initEmpty(argument);
            return emptyPage;
          }
          catch (IOException e) {
            throw new RuntimeException("IO exception during BTree page read: ", e);
          }
        }
      });
    }
    catch (IOException e) {
      throw new RuntimeException(e); //something strange happend
    }
  }

  private long size = -1;
  private long fileSize() throws IOException {
    if(size < 0){
      size = file.size();
    }
//    if(size != file.size())
//     throw new RuntimeException("Fuck");
    return size;
  }


  public boolean containsKey(Object o) {
    return get(o) != null;
  }

  public boolean containsValue(Object o) {
    throw new UnsupportedOperationException();
  }

  public synchronized V get(Object key) {
    //noinspection unchecked
    return valuesCache.get((K)key, new Computable<K, V>() {
      public V compute(K argument) {
        int hashCode = argument.hashCode();
        BTreePage current = pageManager.treeHead();
        while(current instanceof NodePage){
          final NodePage indexPage = (NodePage) current;
          current = getPage(indexPage.find(hashCode));
        }
        final LeafPage leaf = (LeafPage)current;
        final long address = leaf.find(hashCode);
        if(address == 0) return null;
        //noinspection UnnecessaryLocalVariable
        final DataPage data = (DataPage)getPage(address);
        final ByteBuffer buffer = data.readData(address);

        while(buffer.hasRemaining()){
          final K currentKey = keyConverter.convertTo(buffer);
          final V value = valueConverter.convertTo(buffer);
          if(value == null) break;
          if(argument.equals(currentKey)) return value;
        }
        return null;
      }
    });
  }


  Stack<NodePage> parents = new Stack<NodePage>();
  public synchronized V put(K k, V v) {
    final int hashCode = k.hashCode();
    boolean needToNotify = true;
    parents.clear();
    BTreePage current = pageManager.treeHead();
    while(current instanceof NodePage){
      final NodePage indexPage = (NodePage) current;
      parents.push(indexPage);
      current = getPage(indexPage.find(hashCode));
    }
    final LeafPage leaf = (LeafPage)current;
    final long address = leaf.find(hashCode);
    if(address == 0){ // no conflict
      final ByteBuffer keyBuffer = keyConverter.convertFrom(k);
      final ByteBuffer valueBuffer = valueConverter.convertFrom(v);

      final int keyLength = keyBuffer.remaining();
      final byte[] dataChunk = new byte[keyLength + valueBuffer.remaining()];
      keyBuffer.get(dataChunk, 0, keyLength);
      valueBuffer.get(dataChunk, keyLength, valueBuffer.remaining());
      leaf.insert(hashCode, parents, dataChunk);
    }
    else{ // conflict or update
      //noinspection UnnecessaryLocalVariable
      final DataPage data = (DataPage)getPage(address);

      final ByteBuffer keyBuffer = keyConverter.convertFrom(k);
      final ByteBuffer valueBuffer = valueConverter.convertFrom(v);

      final List<ByteBuffer> toWrite = new LinkedList<ByteBuffer>();
      final ByteBuffer currentData = data.readData(address);
      final ByteBuffer buffer = currentData.duplicate();
      int totalLength = keyBuffer.remaining() + valueBuffer.remaining();

      while(buffer.hasRemaining()){
        final int mark = buffer.position();
        final K currentKey = keyConverter.convertTo(buffer);
        final V value = valueConverter.convertTo(buffer);
        if(value == null) break; // skip value contents

        if(!k.equals(currentKey)){
          final int currentChunkLength = buffer.position() - mark;
          toWrite.add((ByteBuffer)buffer.duplicate().limit(buffer.position()).position(mark));
          totalLength += currentChunkLength;
        }
        else{
          if(value.equals(v)) return v; // already have this value
          needToNotify = false;
        }
      }
      final byte[] updatedChunk = new byte[totalLength];
      int index = keyBuffer.remaining();
      keyBuffer.get(updatedChunk, 0, keyBuffer.remaining());
      index += valueBuffer.remaining();
      valueBuffer.get(updatedChunk, index - valueBuffer.remaining(), valueBuffer.remaining());
      for (ByteBuffer currentBuffer : toWrite) {
        index += currentBuffer.remaining();
        currentBuffer.get(updatedChunk, index - currentBuffer.remaining(), currentBuffer.remaining());
      }
      final DataPage newDataPage = pageManager.nextDataPage(updatedChunk.length);
      final long newAddress = newDataPage.writeData(ByteBuffer.wrap(updatedChunk));
      leaf.update(hashCode, newAddress);
    }
    if(needToNotify) pageManager.notifyElementAppend();
    return v;
  }

  public V remove(Object o) {
    throw new UnsupportedOperationException();
  }

  public void putAll(Map<? extends K, ? extends V> map) {
    for (Entry<? extends K, ? extends V> entry : map.entrySet())
      put(entry.getKey(), entry.getValue());
  }

  public void clear() {
    throw new UnsupportedOperationException();
  }

  public Set<K> keySet() {
    throw new UnsupportedOperationException();
  }

  public Collection<V> values() {
    throw new UnsupportedOperationException();
  }

  public Set<Entry<K, V>> entrySet() {
    return new BTreeEntrySet<K,V>(this);
  }

  public ByteBuffer getPageBuffer(int id, boolean write) throws IOException {
    id &= 0x00FFFFFF;
    final long pageOffset = id * SLOT_CAPACITY;
    if (fileSize() < pageOffset + SLOT_CAPACITY)
      throw new IOException("Page is out of boundaries");
    return file.map(write ? FileChannel.MapMode.READ_WRITE : FileChannel.MapMode.READ_ONLY, (long)id * SLOT_CAPACITY, SLOT_CAPACITY);
  }

}
