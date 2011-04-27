package com.spbsu.commons.util;

/**
 * User: solar
 * Date: 02.02.2010
 * Time: 12:28:39
 */
public abstract class RBTreeNodeBase implements RBTreeNode {
  boolean red;
  private RBTreeNode left, right, parent;

  public final boolean isRed() {
    return red;
  }

  @Override
  public final void setRed(boolean red) {
    this.red = red;
  }

  @Override
  public final RBTreeNode left() {
    return left;
  }

  @Override
  public final RBTreeNode right() {
    return right;
  }

  @Override
  public final RBTreeNode parent() {
    return parent;
  }

  @Override
  public final void setLeft(RBTreeNode node) {
    left = node;
  }

  @Override
  public final void setRight(RBTreeNode node) {
    right = node;
  }

  @Override
  public final void setParent(RBTreeNode node) {
    parent = node;
  }
}
