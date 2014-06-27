package com.spbsu.commons.io.persist;

import com.spbsu.commons.func.Computable;
import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.persist.impl.BTreeMapIndex;
import com.spbsu.commons.io.persist.impl.PageFileImpl;
import com.spbsu.commons.util.Flushable;
import com.spbsu.commons.util.cache.CacheStrategy;
import com.spbsu.commons.util.cache.impl.FixedSizeCache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: igorkuralenok
 * Date: 14.10.2009
 * Time: 14:13:57
 */
public class PersistentMap<K, V> implements Flushable {
  PageFile file;
  MapIndex<K> index;
  FixedSizeCache<K, V> cache;
  Map<K, V> increment;
  private final File mapFile;
  private final Converter<V, Buffer> valueConverter;
  private boolean isOpen;
  int size;

  public PersistentMap(File file, Converter<K, Buffer> keyConverter, Converter<V, Buffer> valueConverter, int cacheSize) throws IOException {
    this.mapFile = file;
    this.valueConverter = valueConverter;
    this.file = new PageFileImpl(file);
    this.index = new BTreeMapIndex<K>(this.file, keyConverter);
//    this.index = new PlainMapIndex<K>(this.file, keyConverter);
    this.increment = new HashMap<K, V>();
    cache = new FixedSizeCache<K, V>(cacheSize, CacheStrategy.Type.LRU);
    isOpen = true;
    size = index.size();
  }

  public synchronized int size() {
    checkAccess();
    return size;
  }

  public synchronized boolean isEmpty() {
    checkAccess();
    return size == 0;
  }

  public synchronized boolean containsKey(K k) {
    checkAccess();
    return increment.containsKey(k) || index.get(k) != PageFileAddress.UNKNOWN;
  }

  public synchronized V get(K k) {
    checkAccess();
    autoFlush(false);
    return cache.get(k, new Computable<K, V>() {
      public V compute(K argument) {
        final V v = increment.get(argument);
        if (v != null)
          return v;
        final PageFileAddress address = index.get(argument);
        if (address == null || address == PageFileAddress.UNKNOWN)
          return null;
        return valueConverter.convertFrom(file.read(address));
      }
    });
  }

  public synchronized PersistentMap put(K k, V v) {
    checkAccess();
    if(increment.put(k, v) == null)
      size++;
    cache.put(k, v);
    autoFlush(true);
    return this;
  }

  public synchronized V remove(K k) {
    checkAccess();
    final V old = get(k);
    increment.put(k, null);
    size--;
    cache.clear(k);
    autoFlush(true);
    return old;
  }

  public synchronized Set<K> keySet() {
    final HashSet<K> result = new HashSet<K>(index.keySet());
    result.addAll(increment.keySet());
    return result;
  }

  private int readCount;
  private int writeCount;
  private long lastSaveTime = System.currentTimeMillis();

  private void autoFlush(boolean write) {
    if (write)
      writeCount++;
    else
      readCount++;
//    if ((readCount > 10000 && writeCount > 0) || writeCount > 3000 || increment.size() > 1000 || System.currentTimeMillis() - lastSaveTime > 10000)
    if (increment.size() > 10000 || (System.currentTimeMillis() - lastSaveTime > 60000 && writeCount > 0))
      flush();
  }

  public synchronized void flush() {
    checkAccess();
    for (Map.Entry<K, V> entry : increment.entrySet()) {
      index.set(entry.getKey(),
        entry.getValue() != null
          ? file.write(valueConverter.convertTo(entry.getValue()))
          : PageFileAddress.UNKNOWN
      );
    }
    index.flush();
    file.cleanup();
    readCount = writeCount = 0;
    increment.clear();
    lastSaveTime = System.currentTimeMillis();
    size = index.size();
  }

  public synchronized void close() {
    if (!isOpen) return;
    flush();
    file.close();
    isOpen = false;
  }

  public void drop() throws Exception {
    close();
    if (mapFile.exists() && !mapFile.delete()) {
      Thread.sleep(100); // one more try
      if (!mapFile.delete()) {
        throw new IOException("fail to delete drop map!");
      }
    }
  }

  protected void checkAccess() {
    if (!isOpen) throw new RuntimeException("Map is closed");
  }

  protected void finalize() throws Throwable {
    super.finalize();
    close();
  }
}
