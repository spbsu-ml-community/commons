package com.spbsu.commons.util.tree;

/**
 * User: qdeee
 * Date: 14.07.14
 */
public abstract class Node {
  public Node parent;
  public final int id;

  protected Node(final int id, final Node parent) {
    this.parent = parent;
    this.id = id;
  }

  protected Node(final int id) {
    this(id, null);
  }

  public abstract <R> R accept(final NodeVisitor<R> visitor);

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof Node && this.id == ((Node) obj).id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return "Node id=" + id;
  }
}
