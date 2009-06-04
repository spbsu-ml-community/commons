package com.spbsu.util;

import java.util.ListIterator;

/**
 * @author lawless
 */
public class EmptyListIterator<T> extends EmptyIterator<T> implements ListIterator<T> {
  private static final EmptyListIterator ourInstance = new EmptyListIterator();

  public static synchronized <T> EmptyListIterator<T> emptyListIterator() {
    //noinspection unchecked
    return ourInstance;
  }

  protected EmptyListIterator() {}

  public boolean hasPrevious() {
    return false;
  }

  public T previous() {
    return null;
  }

  public int nextIndex() {
    return -1;
  }

  public int previousIndex() {
    return -1;
  }

  public void set(final T e) {
  }

  public void add(final T e) {
  }
}
