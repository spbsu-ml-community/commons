package com.spbsu.util;

import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.ListIterator;

/**
 * @author lawless
 */
public class EmptyList<T> implements List<T> {
  private static final EmptyList ourInstance = new EmptyList();

  public static <T> EmptyList<T> emptyList() {
    //noinspection unchecked
    return ourInstance;
  }

  protected EmptyList() {}

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
    return EmptyIterator.emptyIterator();
  }

  public Object[] toArray() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public <T>T[] toArray(final T[] a) {
    //noinspection unchecked
    return (T[])ArrayUtil.EMPTY_OBJECT_ARRAY;
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

  public boolean addAll(final int index, final Collection<? extends T> c) {
    return false;
  }

  public boolean removeAll(final Collection<?> c) {
    return false;
  }

  public boolean retainAll(final Collection<?> c) {
    return false;
  }

  public void clear() {
  }

  public T get(final int index) {
    return null;
  }

  public T set(final int index, final T element) {
    return null;
  }

  public void add(final int index, final T element) {
  }

  public T remove(final int index) {
    return null;
  }

  public int indexOf(final Object o) {
    return -1;
  }

  public int lastIndexOf(final Object o) {
    return -1;
  }

  public ListIterator<T> listIterator() {
    return EmptyListIterator.emptyListIterator();
  }

  public ListIterator<T> listIterator(final int index) {
    return EmptyListIterator.emptyListIterator();
  }

  public List<T> subList(final int fromIndex, final int toIndex) {
    return this;
  }
}
