package com.spbsu.commons.util.tree.impl.node;

import com.spbsu.commons.util.tree.Node;
import com.spbsu.commons.util.tree.NodeVisitor;

/**
 * User: qdeee
 * Date: 14.07.14
 */
public class LeafNode extends Node {
  public LeafNode(final int id, final Node parent) {
    super(id, parent);
  }

  public LeafNode(final int id) {
    super(id);
  }

  @Override
  public <R> R accept(final NodeVisitor<R> visitor) {
    return visitor.visit(this);
  }
}
