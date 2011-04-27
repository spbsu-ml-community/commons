package com.spbsu.commons.util;

/**
 * User: solar
 * Date: 01.02.2010
 * Time: 16:54:25
 */
public interface RBTreeNode extends Comparable<RBTreeNode> {
  boolean isRed();
  void setRed(boolean red);

  RBTreeNode left();
  RBTreeNode right();
  RBTreeNode parent();

  void setLeft(RBTreeNode node);
  void setRight(RBTreeNode node);
  void setParent(RBTreeNode node);
}
