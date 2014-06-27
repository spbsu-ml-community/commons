package com.spbsu.commons.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * User: solar
 * Date: 01.02.2010
 * Time: 16:54:06
 */
public class RedBlackTree<T extends RBTreeNode> implements SortedSet<T> {
  RBTreeNode head;
  int size;

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return head == null;
  }

  @Override
  public boolean contains(Object o) {
    RBTreeNode current = head;
    while (current != null && !o.equals(current)) {
      //noinspection unchecked
      current = current.compareTo((T)o) > 0 ? current.left() : current.right();
    }
    return o.equals(current);
  }

  @Override
  public Iterator<T> iterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T[] toArray(T[] ts) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean add(T t) {
    return findOrAdd(t) == t;
  }

  public T findOrAdd(T t) {
    if (head == null) {
      head = t;
      return t;
    }
    RBTreeNode current = head;
    t.setRed(true);
    while (current.compareTo(t) != 0) {
      if (current.compareTo(t) > 0) {
        final RBTreeNode left = current.left();
        if (left == null) {
          current.setLeft(t);
          if (current == first)
            first = t;
          t.setParent(current);
          break;
        }
        else current = left;
      }
      else {
        final RBTreeNode right = current.right();
        if (right == null) {
          current.setRight(t);
          if (current == last)
            last = t;
          t.setParent(current);
          break;
        }
        else current = right;
      }
    }
    if (t.compareTo(current) == 0)
      //noinspection unchecked
      return (T)current;

    insertFixup(t);
    size++;
    return t;
  }

  @Override
  public boolean remove(Object o) {
    if (!(o instanceof RBTreeNode))
      return false;
    RBTreeNode node = (RBTreeNode) o;
    RBTreeNode x, y;

    if(node == first) {
      if (node.right() != null) {
        first = node.right();
        while (first.left() != null)
          first = first().left();
      }
      else first = node.parent();
    }
    if(node == last) {
      if (node.left() != null) {
        last = node.left();
        while (last.right() != null)
          last = first().right();
      }
      else last = node.parent();
    }

    if (node.right() != null && node.left() != null) {
      y = node.right();
      while (y.left() != null)
        y = y.left();
    }
    else y = node;

    if (y.left() != null)
      x = y.left();
    else
      x = y.right();

    if (x != null)
      x.setParent(y.parent());
    if (y.parent() != null) {
      if (y == y.parent().left())
        y.parent().setLeft(x);
      else
        y.parent().setRight(x);
    }
    else
      head = x;
    if (!y.isRed() && x != null)
      deleteFixup(x);
    node.setLeft(null);
    node.setRight(null);
    node.setParent(null);
    node.setRed(false);
    size--;
    return true;
  }

  @Override
  public boolean containsAll(Collection<?> objects) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends T> ts) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> objects) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> objects) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  private void deleteFixup(RBTreeNode x) {
    while (x != head && !x.isRed()) {
      RBTreeNode parent = x.parent();
      if (x == parent.left()) {
        RBTreeNode rightBrother = parent.right();
        if (rightBrother.isRed()) {
          rightBrother.setRed(false);
          parent.setRed(false);
          rotateLeft (parent);
          parent = x.parent();
          rightBrother = parent.right();
        }
        if (rightBrother == null)
          return;
        final RBTreeNode rbLeft = rightBrother.left();
        RBTreeNode rbRight = rightBrother.right();
        if ((rbLeft == null || !rbLeft.isRed()) && (rbRight == null || !rbRight.isRed())) {
          rightBrother.setRed(true);
          x = parent;
        }
        else {
          if (rbRight == null || !rbRight.isRed()) {
            //noinspection ConstantConditions
            rbLeft.setRed(false);
            rightBrother.setRed(true);
            rotateRight (rightBrother);
            parent = x.parent();
            rightBrother = parent.right();
            rbRight = rightBrother.right();
          }
          rightBrother.setRed(parent.isRed());
          parent.setRed(false);
          rbRight.setRed(false);
          rotateLeft (parent);
          x = head;
        }
      }
      else {
        RBTreeNode leftBrother = parent.left();
        if (leftBrother.isRed()) {
          leftBrother.setRed(false);
          parent.setRed(true);
          rotateRight(parent);
          parent = x.parent();
          leftBrother = parent.left();
        }
        final RBTreeNode lbRight = leftBrother.right();
        RBTreeNode lbLeft = leftBrother.left();
        if ((lbRight == null || !lbRight.isRed()) && (lbLeft == null || !lbLeft.isRed())) {
          leftBrother.setRed(true);
          x = parent;
        }
        else {
          if (lbLeft == null || !lbLeft.isRed()) {
            lbRight.setRed(false);
            leftBrother.setRed(true);
            rotateLeft(leftBrother);
            parent = x.parent();
            leftBrother = parent.left();
            lbLeft = leftBrother.left();
          }
          leftBrother.setRed(parent.isRed());
          parent.setRed(false);
          lbLeft.setRed(false);
          rotateRight(parent);
          x = head;
        }
      }
    }
    x.setRed(false);
  }

  private void insertFixup(T t) {
    RBTreeNode x = t;
    RBTreeNode parent;
    while (x != head && (parent = x.parent()).isRed()) {
      /* we have a violation */
      RBTreeNode grandPa = parent.parent();
      if (parent == grandPa.left()) {
        RBTreeNode y = grandPa.right();
        if (y != null && y.isRed()) {
          /* uncle is RED */
          parent.setRed(false);
          y.setRed(false);
          grandPa.setRed(true);
          x = grandPa;
        }
        else {
          /* uncle is BLACK */
          if (x == parent.right()) {
            /* make x a left child */
            x = parent;
            rotateLeft(x);
            parent = x.parent();
            grandPa = parent.parent();
          }

          /* recolor and rotate */
          parent.setRed(false);
          grandPa.setRed(true);
          rotateRight(grandPa);
        }
      }
      else {
        /* mirror image of above code */
        RBTreeNode y = grandPa.left();
        if (y != null && y.isRed()) {
          /* uncle is RED */
          parent.setRed(false);
          y.setRed(false);
          grandPa.setRed(true);
          x = grandPa;
        } else {

          /* uncle is BLACK */
          if (x == parent.left()) {
            x = parent;
            rotateRight(x);
            parent = x.parent();
            grandPa = parent.parent();
          }
          parent.setRed(false);
          grandPa.setRed(true);
          rotateLeft(grandPa);
        }
      }
    }
    head.setRed(false);
  }

  private void rotateRight(RBTreeNode x) {
    RBTreeNode y = x.left();

    x.setLeft(y.right());
    if (y.right() != null)
      y.right().setParent(x);
    y.setRight(x);

    final RBTreeNode parent = x.parent();
    if (parent != null) {
      if (x == parent.right())
        parent.setRight(y);
      else
        parent.setLeft(y);
    }
    else {
      head = y;
    }

    y.setParent(parent);
    x.setParent(y);
  }

  private void rotateLeft(RBTreeNode x) {
    RBTreeNode y = x.right();

    x.setRight(y.left());
    if (y.left() != null)
      y.left().setParent(x);
    y.setLeft(x);

    final RBTreeNode parent = x.parent();
    if (parent != null) {
      if (x == parent.left())
        parent.setLeft(y);
      else
        parent.setRight(y);
    }
    else {
      head = y;
    }

    y.setParent(parent);
    x.setParent(y);
  }

  @Override
  public Comparator<? super T> comparator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedSet<T> subSet(T t, T t1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedSet<T> headSet(T t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedSet<T> tailSet(T t) {
    throw new UnsupportedOperationException();
  }

  private RBTreeNode first, last;

  @Override
  public T first() {
    if (first == null && head != null) {
      first = head;
      while (first.left() != null)
        first = first.left();
    }
    //noinspection unchecked
    return (T)first;
  }

  @Override
  public T last() {
    if (last == null && head != null) {
      last = head;
      while (last.left() != null)
        last = last.right();
    }
    //noinspection unchecked
    return (T)last;
  }

  public T pollFirst() {
    final T result = first();
    if (result != null)
      remove(result);
    return result;
  }
}
