package com.spbsu.commons.io.persist.impl;

import com.spbsu.commons.io.persist.PageFileAddress;

import gnu.trove.iterator.TIntObjectIterator;
import junit.framework.TestCase;

/**
 * User: solar
 * Date: 28.12.2009
 * Time: 18:04:46
 */
public class BTreeTest extends TestCase {
  public void testLeaf () {
    final BTreeNode node = new BTreeLeafNode();
    testNode(node);
  }

  public void testNonLeaf () {
    final BTreeNode node = new BTreeNonLeafNode();
    testNode(node);
  }

  public void testNonLeafReplace () {
    final BTreeNonLeafNode node = new BTreeNonLeafNode();
    short index = 0;
    while (!node.overflow()) {
      node.replace(index*2, new PageFileAddress(index*2 - 1, index, (short)0), new PageFileAddress(index*2 + 1, index, (short)0));
      index++;
    }

    final TIntObjectIterator<PageFileAddress> iterator = node.iterator();
    index = 0;
    while (iterator.hasNext()) {
      iterator.advance();
      if (index == 0) assertEquals(Integer.MIN_VALUE, iterator.key());
      else assertEquals((index - 1) * 2, iterator.key());
      assertEquals(index * 2 - 1, iterator.value().getPageNo());
      if (iterator.hasNext())
        assertEquals(index, iterator.value().getOffset());
      else
        assertEquals(index - 1, iterator.value().getOffset());
      index++;
    }
  }

  private void testNode(BTreeNode node) {
    final PageFileAddress address = new PageFileAddress(1, (short)2, (short)3);
    int index = 0;
    while (!node.overflow()) {
      node.insert(index++ * 100, address);
    }
    final BTreeNodeSplit split = node.split();
    assertEquals((BTreeNode.MAX_ENTRIES + 1) / 2, split.left.size());
    assertEquals((BTreeNode.MAX_ENTRIES + 1) / 2 + (BTreeNode.MAX_ENTRIES + 1) % 2, split.right.size());
    assertEquals((BTreeNode.MAX_ENTRIES + 1) * 50, split.split);
    {
      TIntObjectIterator<PageFileAddress> iterator = split.left.iterator();
      while (iterator.hasNext()) {
        iterator.advance();
        assertTrue(BTreeNode.MAX_ENTRIES * 50 > iterator.key());
        assertEquals(address, iterator.value());
      }
    }
    {
      TIntObjectIterator<PageFileAddress> iterator = split.right.iterator();
      while (iterator.hasNext()) {
        iterator.advance();
        assertTrue(BTreeNode.MAX_ENTRIES * 50 <= iterator.key());
        assertEquals(address, iterator.value());
      }
    }
  }
}
