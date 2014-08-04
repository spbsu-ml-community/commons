package com.spbsu.commons.util;

import com.spbsu.commons.util.tree.FastTree;
import com.spbsu.commons.util.tree.impl.node.InternalNode;
import com.spbsu.commons.util.tree.impl.node.LeafNode;
import junit.framework.TestCase;

/**
 * User: qdeee
 * Date: 25.07.14
 */
public class TreeTest extends TestCase {
  public void testFastTree() throws Exception {
    //    0
    //1   2   3
    //      4   5

    final InternalNode node = new InternalNode(3);
    node.addChild(new LeafNode(4));
    node.addChild(new LeafNode(5));

    final InternalNode root = new InternalNode(0);
    root.addChild(new LeafNode(1));
    root.addChild(new LeafNode(2));
    root.addChild(node);

    final FastTree tree = new FastTree(root);

    assertTrue(tree.isFirstDescendantOfSecondOrEqual(0, 0));
    assertTrue(tree.isFirstDescendantOfSecondOrEqual(1, 1));
    assertTrue(tree.isFirstDescendantOfSecondOrEqual(4, 0));
    assertTrue(tree.isFirstDescendantOfSecondOrEqual(3, 0));
    assertTrue(tree.isFirstDescendantOfSecondOrEqual(1, 0));
    assertFalse(tree.isFirstDescendantOfSecondOrEqual(0, 1));
    assertFalse(tree.isFirstDescendantOfSecondOrEqual(1, 5));
  }
}
