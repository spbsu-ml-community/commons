package com.spbsu.commons.io.persist.impl;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;
import com.spbsu.commons.io.persist.PageFileAddress;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
* User: solar
* Date: 28.12.2009
* Time: 15:16:56
*/

class BTreeNodeSplit {
  int split;
  BTreeNode left;
  BTreeNode right;
}

class Counter {
  static int counter = 0;
  Counter() {
    counter++;
  }
}

public interface BTreeNode {
  Converter<BTreeNode, Buffer> CONVERTER = new BTreeNodeConverter();
  int HEADER_SIZE = 16;
  int ENTRY_SIZE = 4 + 12;
//  int MAX_ENTRIES = 127;
  int MAX_ENTRIES = (PageFileImpl.PAGE_SIZE - HEADER_SIZE) / ENTRY_SIZE;

  boolean overflow();
  BTreeNodeSplit split();

  PageFileAddress address();
  void address(PageFileAddress address);

  void insert(int hash, PageFileAddress pageFileAddress);
  PageFileAddress find(int hash);

  TIntObjectIterator<PageFileAddress> iterator();
  int size();
}

class BTreeNonLeafNode extends Counter implements BTreeNode {
  final TreeMap<Integer, PageFileAddress> contents = new TreeMap<Integer, PageFileAddress>();
  PageFileAddress address;

  public PageFileAddress address() {
    return address;
  }

  public void address(PageFileAddress address) {
    this.address = address;
  }

  public PageFileAddress find(int hash) {
    final Map.Entry<Integer, PageFileAddress> addressEntry = contents.floorEntry(hash);
    return addressEntry.getValue();
  }

  public void insert(int hash, PageFileAddress pageFileAddress) {
    contents.put(hash, pageFileAddress);
  }

  public void replace(int split, PageFileAddress left, PageFileAddress right) {
    if(!contents.isEmpty()) {
      final Integer oldNode = contents.floorKey(split);
      contents.put(oldNode, left);
      contents.put(split, right);
    }
    else {
      contents.put(Integer.MIN_VALUE, left);
      contents.put(split, right);
    }
  }

  public boolean overflow() {
    return contents.size() > MAX_ENTRIES;
  }

  public BTreeNodeSplit split() {
    BTreeNodeSplit result = new BTreeNodeSplit();
    BTreeNonLeafNode left, right;
    result.left = left = new BTreeNonLeafNode();
    result.right = right = new BTreeNonLeafNode();
    int index = 0;
    final int middle = contents.size() / 2;
    for (Map.Entry<Integer, PageFileAddress> entry : contents.entrySet()) {
      if (index > middle)
        right.insert(entry.getKey(), entry.getValue());
      else if (index < middle)
        left.insert(entry.getKey(), entry.getValue());
      else {
        result.split = entry.getKey();
        right.insert(result.split, entry.getValue());
      }
      index++;
    }
    return result;
  }

  static TIntObjectHashMap<PageFileAddress> empty = new TIntObjectHashMap<PageFileAddress>();
  public TIntObjectIterator<PageFileAddress> iterator() {
    final Iterator<Map.Entry<Integer, PageFileAddress>> iter = contents.entrySet().iterator();
    return new TIntObjectIterator<PageFileAddress>(empty) {
      public Map.Entry<Integer, PageFileAddress> value;
      @Override
      public boolean hasNext() {
        return iter.hasNext();
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
      @Override
      public void advance() {
        value = iter.next();
      }
      @Override
      public int key() {
        return value.getKey();
      }
      @Override
      public PageFileAddress value() {
        return value.getValue();
      }
      @Override
      public PageFileAddress setValue(PageFileAddress val) {
        throw new UnsupportedOperationException();
      }
    };
  }

  public int size() {
    return contents.size();
  }
}

class BTreeLeafNode extends Counter implements BTreeNode {
  final TIntObjectHashMap<PageFileAddress> contents = new TIntObjectHashMap<PageFileAddress>();
  PageFileAddress address;

  public PageFileAddress address() {
    return address;
  }

  public void address(PageFileAddress address) {
    this.address = address;
  }

  public TIntObjectIterator<PageFileAddress> iterator() {
    return contents.iterator();
  }

  public int size() {
    return contents.size();
  }

  public PageFileAddress find(int hash) {
    final PageFileAddress address = contents.get(hash);
    return address != null ? address : PageFileAddress.UNKNOWN;
  }

  public void insert(int hash, PageFileAddress pageFileAddress) {
    contents.put(hash, pageFileAddress);
  }

  public boolean overflow() {
    return contents.size() > MAX_ENTRIES;
  }

  public BTreeNodeSplit split() {
    BTreeNodeSplit split = new BTreeNodeSplit();
    BTreeLeafNode left, right;
    split.left = left = new BTreeLeafNode();
    split.right = right = new BTreeLeafNode();
    final int[] hashes = contents.keys();
    Arrays.sort(hashes);
    final int middle = contents.size() / 2;

    for (int i = 0; i < hashes.length; i++) {
      if (i < middle)
        left.insert(hashes[i], contents.get(hashes[i]));
      else
        right.insert(hashes[i], contents.get(hashes[i]));
    }
    split.split = hashes[middle];
    return split;
  }
}

class BTreeNodeConverter implements Converter<BTreeNode, Buffer>{
  public BTreeNode convertFrom(Buffer source) {
    final int size = source.getInt();
    final BTreeNode ret;
    if(source.getInt() == 0) ret = new BTreeNonLeafNode();
    else ret = new BTreeLeafNode();
    for (int i = 0; i < size; i++) {
      ret.insert(source.getInt(), PageFileAddress.CONVERTER.convertFrom(source));
    }
    return ret;
  }

  public Buffer convertTo(BTreeNode object) {
    final Buffer result = BufferFactory.wrap(new byte[BTreeNode.ENTRY_SIZE * BTreeNode.MAX_ENTRIES + BTreeNode.HEADER_SIZE]);
    final TIntObjectIterator<PageFileAddress> iter = object.iterator();
    result.putInt(object.size());
    if (object instanceof BTreeNonLeafNode) result.putInt(0);
    else result.putInt(1);

    while (iter.hasNext()) {
      iter.advance();
      result.putInt(iter.key());
      BufferFactory.write(PageFileAddress.CONVERTER.convertTo(iter.value()), result);
    }
    result.position(0);
    return result;
  }
}
