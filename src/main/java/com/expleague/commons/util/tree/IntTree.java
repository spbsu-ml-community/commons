package com.expleague.commons.util.tree;

import gnu.trove.iterator.TIntIterator;
import org.jetbrains.annotations.Nullable;

/**
 * User: qdeee
 * Date: 07.08.14
 */
public interface IntTree {
  public static final int ROOT = 0;

  public static enum TRAVERSE_STRATEGY {
    DEPTH_FIRST,
    BREADTH_FIRST
  }

  /**
   * Get total nodes count
   * @return nodes count
   */
  int nodesCount();

  /**
   * Get parent of specified node
   * @param node
   * @return
   */
  int getParent(int node);

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

  int childrenCount(int node);

  /**
   * Lightweight children iterator
   * @param node
   * @return children iterator
   */
  @Nullable
  TIntIterator getChildren(int node);

  /**
   * @param node
   * @param parent
   * @return true if 'node' is descendant of 'parent', otherwise false
   */
  boolean isDescendant(int node, int parent);

  int[] traversal(final TRAVERSE_STRATEGY strategy);
  int[] leaves(final TRAVERSE_STRATEGY strategy);
  int[] internals(final TRAVERSE_STRATEGY strategy);

  void accept(final IntTreeVisitor visitor, final int node);
}
