package com.spbsu.util;

import java.util.*;

/**
 * @author lawless
 */
public class SingletonList<T> implements List<T> {
  private final T element;

  public static <T> SingletonList<T> singletonList(final T element) {
    return new SingletonList<T>(element);
  }

  private SingletonList(final T element) {
    this.element = element;
  }

  public int size() {
    return 1;
  }

  public boolean isEmpty() {
    return false;
  }

  public boolean contains(final Object o) {
    return element.equals(o);
  }

  public Iterator<T> iterator() {
    return listIterator();
  }

  public Object[] toArray() {
    return new Object[] {element};
  }

  public <T>T[] toArray(final T[] a) {
    final Object[] original = toArray();
    if (a.length < 1) {
      // Make a new array of a's runtime type, but my contents:
      // noinspection unchecked
      return (T[]) Arrays.copyOf(original, 1, a.getClass());
    }
    System.arraycopy(original, 0, a, 0, 1);
    if (a.length > 1) {
      a[1] = null;
    }
    return a;
  }

  public boolean add(final T e) {
    throw new UnsupportedOperationException();
  }

  public boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  public boolean containsAll(final Collection<?> c) {
    return c.size() == 1 && c.contains(element);
  }

  public boolean addAll(final Collection<? extends T> c) {
    throw new UnsupportedOperationException();
  }

  public boolean addAll(final int index, final Collection<? extends T> c) {
    throw new UnsupportedOperationException();
  }

  public boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  public boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  public void clear() {
    throw new UnsupportedOperationException();
  }

  public T get(final int index) {
    return index == 0 ? element : null;
  }

  public T set(final int index, final T element) {
    throw new UnsupportedOperationException();
  }

  public void add(final int index, final T element) {
    throw new UnsupportedOperationException();
  }

  public T remove(final int index) {
    throw new UnsupportedOperationException();
  }

  public int indexOf(final Object o) {
    return element.equals(o) ? 0 : -1;
  }

  public int lastIndexOf(final Object o) {
    return element.equals(o) ? 0 : -1;
  }

  public ListIterator<T> listIterator() {
    return new ListIterator<T>() {
      private boolean atStart = true;

      public boolean hasNext() {
        return atStart;
      }

      public T next() {
        atStart = false;
        return element;
      }

      public boolean hasPrevious() {
        return !atStart;
      }

      public T previous() {
        return atStart ? null : element;
      }

      public int nextIndex() {
        return atStart ? 0 : 1;
      }

      public int previousIndex() {
        return atStart ? -1 : 0;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

      public void set(final T e) {
        throw new UnsupportedOperationException();
      }

      public void add(final T e) {
        throw new UnsupportedOperationException();
      }
    };
  }

  public ListIterator<T> listIterator(final int index) {
    return index == 0 ? listIterator() : null;
  }

  public List<T> subList(final int fromIndex, final int toIndex) {
    return fromIndex == 0 && toIndex == 1 ? this : null;
  }
}
