package com.spbsu.commons.util.tree;

import com.spbsu.commons.util.tree.impl.node.InternalNode;
import com.spbsu.commons.util.tree.impl.node.LeafNode;

/**
 * User: qdeee
 * Date: 14.07.14
 */
public class Tree {
  protected Node root;

  public Tree(final Node root) {
    this.root = root;
  }

  public Node getRoot() {
    return root;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    root.accept(new NodeVisitor<Void>() {
      @Override
      public Void visit(final InternalNode node) {
        builder.append("Internal node #").append(node.id).append("\n");
        for (Node child : node.getChildren()) {
          child.accept(this);
        }
        return null;
      }

      @Override
      public Void visit(final LeafNode node) {
        builder.append("Leaf node #").append(node.id).append("\n");
        return null;
      }
    });
    return builder.toString();
  }
}
