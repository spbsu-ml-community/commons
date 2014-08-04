package com.spbsu.commons.util.tree;

import com.spbsu.commons.util.tree.impl.node.InternalNode;
import com.spbsu.commons.util.tree.impl.node.LeafNode;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * User: qdeee
 * Date: 24.07.14
 */
public class FastTree extends Tree {
  final TIntObjectMap<Node> id2node = new TIntObjectHashMap<>();

  public FastTree(final Node root) {
    super(root);
    final NodeVisitor nodeAdder = new NodeVisitor() {
      @Override
      public Void visit(final InternalNode node) {
        id2node.put(node.id, node);
        for (Node child : node.getChildren()) {
          child.accept(this);
        }
        return null;
      }

      @Override
      public Void visit(final LeafNode node) {
        id2node.put(node.id, node);
        return null;
      }
    };
    root.accept(nodeAdder);
  }

  public boolean isFirstDescendantOfSecondOrEqual(int id1, int id2) {
    final Node node1 = id2node.get(id1);
    final Node node2 = id2node.get(id2);
    Node parent = node1;
    while (parent != null) {
      if (parent.equals(node2))
        return true;
      parent = parent.parent;
    }
    return false;
  }
}
