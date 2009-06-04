package com.spbsu.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author lawless
 */
public final class SingletonSet<T> implements Set<T> {
  private final T myValue;
  private final Object[] myArray;

  public static final <T> SingletonSet<T> singleton(final T value) {
    return new SingletonSet<T>(value);
  }

  private SingletonSet(final T value) {
    myValue = value;
    myArray = new Object[] {myValue};
  }

  public int size() {
    return 1;
  }

  public boolean isEmpty() {
    return false;
  }

  public boolean contains(final Object o) {
    return myValue == null ? o == null : myValue.equals(o);
  }

  public Iterator<T> iterator() {
    return new MyIterator();
  }

  public Object[] toArray() {
    return myArray;
  }

  public <Q>Q[] toArray(final Q[] a) {
    final int size = size();
    final Q[] r = a.length >= size ? a : (Q[]) java.lang.reflect.Array .newInstance(a.getClass().getComponentType(), size);
    //noinspection unchecked
    r[0] = (Q) myValue;
    return r;
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

  private final class MyIterator implements Iterator<T> {
    private boolean myHasNext = true;
    public boolean hasNext() {
      return myHasNext;
    }

    public T next() {
      if (myHasNext) {
        myHasNext = false;
        return myValue;
      }
      throw new NoSuchElementException();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
