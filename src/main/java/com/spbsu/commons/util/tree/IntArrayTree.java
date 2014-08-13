package com.spbsu.commons.util.tree;

import com.spbsu.commons.util.ArrayTools;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import org.jetbrains.annotations.Nullable;

/**
 * User: qdeee
 * Date: 01.08.14
 */

public class IntArrayTree implements IntTree {
  /**
   * Indexes of children.
   * Node #i has next children labels interval: [children[i-1], children[i]). For definitely, children[-1] = 1.
   */
  private final TIntList children = new TIntArrayList();

  public IntArrayTree() {
    children.add(1);
  }

  @Override
  public int addTo(int parent) {
    checkIndex(parent);

    final int endChildren = children.get(parent);
    children.add(children.size());
    for (int i = children.size() - 1; i >= endChildren; i--) {
      children.set(i, children.get(i - 1) + 1);
    }
    for (int i = endChildren - 1; i >= parent; i--) {
      children.set(i, children.get(i) + 1);
    }
    return endChildren;
  }

  @Override
  public boolean hasChildren(int node) {
    return childrenCount(node) > 0;
  }

  @Override
  public int getParent(int node) {
    checkIndex(node);
    for (int i = node - 1; i > 0; i--) {
      int startChildren = children.get(i - 1);
      int endChildren = children.get(i);
      if (startChildren <= node && node < endChildren)
        return i;
    }
    return node != ROOT ? ROOT : -1;
  }

  @Override
  public int addToRoot() {
    return addTo(ROOT);
  }

  @Override
  public int childrenCount(final int node) {
    final int startChildren = node != ROOT ? children.get(node - 1) : 1;
    final int endChildren = children.get(node);
    return endChildren - startChildren;
  }

  private void checkIndex(int i) {
    if (i >= children.size() || i < 0) {
      throw new IndexOutOfBoundsException("Node with label " + i + " not found.");
    }
  }

  @Override
  public boolean isDescendant(int node, int parent) {
    return find(parent, node);
  }

  private boolean find(int startNode, int label) {
    final int startChildren = startNode > 0 ? children.get(startNode - 1) : 1;
    final int endChildren = children.get(startNode);
    for (int i = startChildren; i < endChildren; i++) {
      if (i == label || find(i, label))
        return true;
    }
    return false;
  }

  @Override
  public int nodesCount() {
    return children.size();
  }

  @Override
  @Nullable
  public TIntIterator getChildren(final int node) {
    checkIndex(node);
    final int startChildren = node != ROOT ? children.get(node - 1) : 1;
    final int endChildren = children.get(node);
    if (endChildren - startChildren > 0) {
      return new TIntIterator() {
        int index = startChildren;

        @Override
        public int next() {
          return index++;
        }

        @Override
        public boolean hasNext() {
          return index < endChildren;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
    else {
      return null;
    }
  }

  @Override
  public void accept(final IntTreeVisitor visitor, final int node) {
    visitor.visit(node);
  }

  @Override
  public int[] traversal(final TRAVERSE_STRATEGY strategy) {
    switch (strategy) {
      case BREADTH_FIRST:
        return ArrayTools.sequence(0, children.size());
      case DEPTH_FIRST:
        return depthFirstTraversal(ROOT);
      default:
        throw new IllegalArgumentException();
    }
  }

  private int[] depthFirstTraversal(int from) {
    final TIntList result = new TIntLinkedList();
    accept(new IntTreeVisitor() {
      @Override
      public void visit(final int node) {
        result.add(node);
        final TIntIterator nodeChildren = getChildren(node);
        if (nodeChildren != null) {
          while (nodeChildren.hasNext()) {
            accept(this, nodeChildren.next());
          }
        }
      }
    }, from);
    return result.toArray();
  }

  public int[] leaves(final TRAVERSE_STRATEGY strategy) {
    switch (strategy) {
      case BREADTH_FIRST: return breadthFirstLeaves();
      case DEPTH_FIRST:   return depthFirstLeaves();
      default:
        throw new IllegalArgumentException();
    }
  }

  private int[] depthFirstLeaves() {
    final TIntList result = new TIntLinkedList();
    accept(new IntTreeVisitor() {
      @Override
      public void visit(final int node) {
        final TIntIterator nodeChildren = getChildren(node);
        if (nodeChildren != null) {
          while (nodeChildren.hasNext()) {
            accept(this, nodeChildren.next());
          }
        } else {
          result.add(node);
        }
      }
    }, ROOT);
    return result.toArray();
  }

  private int[] breadthFirstLeaves() {
    final TIntList result = new TIntLinkedList();
    for (int i = 0; i < nodesCount(); i++) {
      if (!hasChildren(i)) {
        result.add(i);
      }
    }
    return result.toArray();
  }

  @Override
  public int[] internals(final TRAVERSE_STRATEGY strategy) {
    switch (strategy) {
      case BREADTH_FIRST: return breadthFirstInternals();
      case DEPTH_FIRST: return deepFirstInternals();
      default:
        throw new IllegalArgumentException();

    }
  }

  private int[] breadthFirstInternals() {
    final TIntList result = new TIntLinkedList();
    for (int i = 0; i < nodesCount(); i++) {
      if (hasChildren(i)) {
        result.add(i);
      }
    }
    return result.toArray();
  }

  private int[] deepFirstInternals() {
    final TIntList result = new TIntLinkedList();
    accept(new IntTreeVisitor() {
      @Override
      public void visit(final int node) {
        final TIntIterator nodeChildren = getChildren(node);
        if (nodeChildren != null) {
          result.add(node);
          while (nodeChildren.hasNext()) {
            accept(this, nodeChildren.next());
          }
        }
      }
    }, ROOT);
    return result.toArray();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    for (int i = 0; i < children.size(); i++) {
      final int start = i != ROOT ? children.get(i - 1) : 1;
      final int end = children.get(i);
      if (end - start > 0) {
        builder.append("Node #").append(i).append(" has children with labels: [");
        for (int j = start; j < end; j++) {
          builder.append(j);
          builder.append(", ");
        }
        builder.append("]\n");
      }
      else {
        builder.append("Node #");
        builder.append(i);
        builder.append(" is a leaf\n");
      }
    }
    return builder.toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof IntArrayTree)) return false;

    final IntArrayTree that = (IntArrayTree) o;

    if (!children.equals(that.children)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return children.hashCode();
  }
}
