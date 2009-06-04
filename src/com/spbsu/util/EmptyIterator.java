package com.spbsu.util;

import java.util.Iterator;

/**
 * @author lawless
 */
public class EmptyIterator<T> implements Iterator<T> {
  private static final EmptyIterator ourInstance = new EmptyIterator();

  public static synchronized <T> EmptyIterator<T> emptyIterator() {
    //noinspection unchecked
    return ourInstance;
  }

  protected EmptyIterator() {}

  public boolean hasNext() {
    return false;
  }

  public T next() {
    return null;
  }

  public void remove() {}
}
