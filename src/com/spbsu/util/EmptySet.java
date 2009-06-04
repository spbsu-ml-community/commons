package com.spbsu.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * @author lawless
 */
public final class EmptySet<T> implements Set<T> {
  private static final EmptySet ourInstance = new EmptySet();

  public static <T> EmptySet<T> emptySet() {
    //noinspection unchecked
    return ourInstance;
  }

  private EmptySet() {}

  public int size() {
    return 0;
  }

  public boolean isEmpty() {
    return true;
  }

  public boolean contains(final Object o) {
    return false;
  }

  public Iterator<T> iterator() {
    //noinspection unchecked
    return EmptyIterator.ourInstance;
  }

  public Object[] toArray() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public <T>T[] toArray(final T[] a) {
    return a;
  }

  public boolean add(final T e) {
    return false;
  }

  public boolean remove(final Object o) {
    return false;
  }

  public boolean containsAll(final Collection<?> c) {
    return false;
  }

  public boolean addAll(final Collection<? extends T> c) {
    return false;
  }

  public boolean retainAll(final Collection<?> c) {
    return false;
  }

  public boolean removeAll(final Collection<?> c) {
    return false;
  }

  public void clear() {}

  private final static class EmptyIterator<T> implements Iterator<T> {
    private static final EmptyIterator ourInstance = new EmptyIterator();
    public boolean hasNext() {
      return false;
    }

    public T next() {
      return null;
    }

    public void remove() {}
  }
}
