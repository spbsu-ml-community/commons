package com.spbsu.commons.util.tree.impl.node;

import com.spbsu.commons.util.tree.Node;
import com.spbsu.commons.util.tree.NodeVisitor;

import java.util.LinkedList;
import java.util.List;

/**
 * User: qdeee
 * Date: 14.07.14
 */
public class InternalNode extends Node {
  protected final List<Node> children;

  public InternalNode(final int id, final Node parent) {
    super(id, parent);
    this.children = new LinkedList<>();
  }

  public InternalNode(final int id) {
    this(id, null);
  }

  public void addChild(Node node) {
    children.add(node);
    node.parent = this;
  }

  public List<Node> getChildren() {
    return children;
  }

  @Override
  public <R> R accept(final NodeVisitor<R> visitor) {
    return visitor.visit(this);
  }
}
