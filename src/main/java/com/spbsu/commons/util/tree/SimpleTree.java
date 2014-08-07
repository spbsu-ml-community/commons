package com.spbsu.commons.util.tree;

import gnu.trove.iterator.TIntIterator;
import org.jetbrains.annotations.Nullable;

/**
 * User: qdeee
 * Date: 07.08.14
 */
public interface SimpleTree {
  /**
   * Add new node to the specified parent node.
   * @param parent parent node label
   * @return new node's label
   */
  int addTo(int parent);

  /**
   * Add new node to the root node.
   * @return new node's label
   */
  int addToRoot();

  boolean hasChildren(int node);

  /**
   * Get parent of specified node
   * @param node
   * @return
   */
  int getParent(int node);

  /**
   * @param node
   * @param parent
   * @return true if 'node' is descendant of 'parent', otherwise false
   */
  boolean isDescendant(int node, int parent);

  /**
   * Get total nodes count
   * @return nodes count
   */
  int nodesCount();

  /**
   * Lightweight children iterator
   * @param node
   * @return children iterator
   */
  @Nullable
  TIntIterator getChildren(int node);
}
