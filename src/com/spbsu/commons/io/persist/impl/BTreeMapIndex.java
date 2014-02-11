package com.spbsu.commons.io.persist.impl;

import com.spbsu.commons.func.Computable;
import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;
import com.spbsu.commons.io.persist.MapIndex;
import com.spbsu.commons.io.persist.PageFile;
import com.spbsu.commons.io.persist.PageFileAddress;
import com.spbsu.commons.util.cache.Cache;
import com.spbsu.commons.util.cache.CacheStrategy;
import com.spbsu.commons.util.cache.impl.FixedSizeCache;

import gnu.trove.iterator.TIntObjectIterator;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: igorkuralenok
 * Date: 14.10.2009
 * Time: 15:10:44
 */
public class BTreeMapIndex<K> implements MapIndex<K> {
  private final Cache<K, PageFileAddress> addresses = new FixedSizeCache<K, PageFileAddress>(10000, CacheStrategy.Type.LRU);
  private BTreeNode rootPage;
  private final PageFile file;
  private final Converter<K, Buffer> keyConverter;
  private final Map<PageFileAddress, BTreeNode> changedNodes = new HashMap<PageFileAddress, BTreeNode>();
  private final Cache<PageFileAddress, BTreeNode> nodesCache = new FixedSizeCache<PageFileAddress, BTreeNode>(400, CacheStrategy.Type.LRU);
  private int size;
  private static final int ROOT_PAGE_ADDRESS_SLOT = 1;
  private static final int SIZE_SLOT = 2;

  public BTreeMapIndex(PageFile file, Converter<K, Buffer> keyConverter) {
    this.file = file;
    this.keyConverter = keyConverter;
    if (file.size() > 1) {
      rootPage = getNode(file.readHeader(ROOT_PAGE_ADDRESS_SLOT));
      size = file.readHeader(SIZE_SLOT).getPageNo();
    }
    else {
      rootPage = new BTreeLeafNode();
      rootPage.address(file.write(BTreeNode.CONVERTER.convertTo(rootPage)));
      file.writeHeader(ROOT_PAGE_ADDRESS_SLOT, rootPage.address());
      file.writeHeader(SIZE_SLOT, new PageFileAddress(0, (short)0, (short)0));
    }
  }

  public PageFileAddress get(K key) {
    return addresses.get(key, new Computable<K, PageFileAddress>() {
      public PageFileAddress compute(K key) {
        final int hash = key.hashCode();
        PageFileAddress address = findLeaf(hash, null).find(hash);
        if (address == PageFileAddress.UNKNOWN)
          return PageFileAddress.UNKNOWN;
        final Buffer buffer = file.read(address);
        while (buffer.remaining() > 0) {
          final K k = keyConverter.convertFrom(buffer);
          final PageFileAddress dataAddress = PageFileAddress.CONVERTER.convertFrom(buffer);
          if (key.equals(k))
            return dataAddress;
        }
        return PageFileAddress.UNKNOWN;
      }
    });
  }

  public void set(K key, PageFileAddress newAddress) {
    addresses.put(key, newAddress);
    final int hash = key.hashCode();
    final LinkedList<BTreeNode> parents = new LinkedList<BTreeNode>();
    final BTreeLeafNode leaf = findLeaf(hash, parents);
    final PageFileAddress existing = leaf.find(hash);
    if (existing != PageFileAddress.UNKNOWN) { // reuse existing value
      final Buffer buffer = file.read(existing);
      int bufferStart = buffer.position();
      while (buffer.remaining() > 0) {
        final K k = keyConverter.convertFrom(buffer);
        if (key.equals(k)) { // change referencee and exit
          final int addrStart = buffer.position();
          file.enqueueForCleanup(PageFileAddress.CONVERTER.convertFrom(buffer));
          buffer.position(addrStart);
          BufferFactory.write(PageFileAddress.CONVERTER.convertTo(newAddress), buffer);
          return;
        }
        else PageFileAddress.CONVERTER.convertFrom(buffer);
      }
      buffer.position(bufferStart);
      leaf.insert(hash, file.write(
          BufferFactory.join(buffer, keyConverter.convertTo(key), PageFileAddress.CONVERTER.convertTo(newAddress))
      ));
      file.enqueueForCleanup(existing);
      changedNodes.put(leaf.address, leaf);
    }
    else { // insert value to the leaf
      leaf.insert(hash, file.write(
          BufferFactory.join(keyConverter.convertTo(key), PageFileAddress.CONVERTER.convertTo(newAddress))
      ));
      changedNodes.put(leaf.address, leaf);

      BTreeNode current = leaf;
      Iterator<BTreeNode> parentsIterator = parents.descendingIterator();
      while (current.overflow()) {
        BTreeNodeSplit split = current.split();
        final PageFileAddress oldAddress = current.address();
        file.enqueueForCleanup(oldAddress);
        changedNodes.remove(oldAddress);
        nodesCache.clear(oldAddress);
        if (!parentsIterator.hasNext()) {
          current = rootPage = new BTreeNonLeafNode();
          final PageFileAddress rootAddress = file.write(BTreeNode.CONVERTER.convertTo(current));
          rootPage.address(rootAddress);
          file.writeHeader(ROOT_PAGE_ADDRESS_SLOT, rootAddress);
        }
        else current = parentsIterator.next();
        split.left.address(file.write(BTreeNode.CONVERTER.convertTo(split.left)));
        split.right.address(file.write(BTreeNode.CONVERTER.convertTo(split.right)));
        ((BTreeNonLeafNode)current).replace(split.split, split.left.address(), split.right.address());
        changedNodes.put(current.address(), current);
      }
    }
    file.writeHeader(SIZE_SLOT, new PageFileAddress(++size, (short)0, (short)0));
  }

  private BTreeLeafNode findLeaf(int hash, @Nullable List<BTreeNode> parents) {
    BTreeNode current = rootPage;
    PageFileAddress address;
    while (true) {
      address = current.find(hash);
      if (current instanceof BTreeLeafNode)
        return (BTreeLeafNode)current;
      BTreeNode next = getNode(address);
      if (parents != null)
        parents.add(current);
      current = next;
    }
  }

  private BTreeNode getNode(PageFileAddress address) {
    return nodesCache.get(address, new Computable<PageFileAddress, BTreeNode>() {
      public BTreeNode compute(PageFileAddress address) {
        BTreeNode next = changedNodes.get(address);
        if (next == null) {
          next = BTreeNode.CONVERTER.convertFrom(file.read(address));
          next.address(address);
        }
        return next;
      }
    });
  }

  public void flush() {
    for (Map.Entry<PageFileAddress, BTreeNode> entry : changedNodes.entrySet()) {
      BufferFactory.write(BTreeNode.CONVERTER.convertTo(entry.getValue()), file.read(entry.getKey()));
    }
    changedNodes.clear();
  }

  public int size() {
    return size;
  }

  public Set<K> keySet() {
    final Set<K> keys = new HashSet<K>();
    if (rootPage instanceof BTreeLeafNode) {
      processLeaf((BTreeLeafNode) rootPage, keys);
    }
    else {
      Set<BTreeNonLeafNode> front = new HashSet<BTreeNonLeafNode>();
      processNonLeaf((BTreeNonLeafNode) rootPage, keys, front);
      while (!front.isEmpty()) {
        final Set<BTreeNonLeafNode> newFront = new HashSet<BTreeNonLeafNode>();
        for (final BTreeNonLeafNode node : front) {
          processNonLeaf(node, keys, newFront);
        }
        front = newFront;
      }
    }
    return keys;
  }

  private void processNonLeaf(final BTreeNonLeafNode node, final Set<K> keys, final Set<BTreeNonLeafNode> nodes) {
    final TIntObjectIterator<PageFileAddress> it = node.iterator();
    while (it.hasNext()) {
      it.advance();
      final PageFileAddress address = it.value();
      if (address == PageFileAddress.UNKNOWN) continue;

      final BTreeNode child = getNode(address);
      if (child instanceof BTreeLeafNode) processLeaf((BTreeLeafNode) child, keys);
      else nodes.add((BTreeNonLeafNode) child);
    }
  }

  private void processLeaf(final BTreeLeafNode node, final Set<K> keys) {
    final TIntObjectIterator<PageFileAddress> it = node.iterator();
    while (it.hasNext()) {
      it.advance();
      final PageFileAddress address = it.value();
      if (address == PageFileAddress.UNKNOWN) continue;

      final Buffer buffer = file.read(address);
      final int start = buffer.position();
      while (buffer.remaining() > 0) {
        keys.add(keyConverter.convertFrom(buffer));
        PageFileAddress.CONVERTER.convertFrom(buffer);
      }
      buffer.position(start);
    }
  }
}