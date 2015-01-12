package com.spbsu.commons.math.vectors.impl.basis;


import com.spbsu.commons.math.vectors.GenericBasis;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/* Created with IntelliJ IDEA.
 * ksen | 18:34 26.02.2013 | commons
 */

// Cann't contain NULL. Each element in basis is unique.
public class MapBasis<T> implements GenericBasis<T> {

  private final TObjectIntHashMap<T> map;
  private final List<T> inverted;

  public MapBasis() {
    map = new TObjectIntHashMap<T>();
    inverted = new ArrayList<T>();
  }

  public MapBasis(final int capacity) {
    map = new TObjectIntHashMap<T>(capacity);
    inverted = new ArrayList<T>(capacity);
  }

  public MapBasis(final T[] basis) {
    this(basis.length);
    final int size = basis.length;
    for (int i = 0; i < size; i++) {
      add(basis[i]);
    }
    if(map.size() != basis.length)
      throw new RuntimeException(new IllegalArgumentException("Basis must contain unique keys."));
  }

  public MapBasis(final Collection<T> basis) {
    this(basis.size());
    final int size = basis.size();
    final Iterator<T> iterator = basis.iterator();
    for (int i = 0; i < size; i++) {
      add(iterator.next());
    }
    if(map.size() != basis.size())
      throw new RuntimeException(new IllegalArgumentException("Basis must contain unique keys."));
  }

  public MapBasis(final GenericBasis<T> basis) {
    this(basis.size());
    final int size = basis.size();
    for (int i = 0; i < size; i++) {
      add(basis.fromIndex(i));
    }
  }

  @Override public synchronized int add(@NotNull final T element) {
    if(map.containsKey(element))
      return map.get(element) - 1;
    else {
      map.put(element, inverted.size() + 1);
      inverted.add(element);
      return inverted.size() - 1;
    }
  }

  @Override public synchronized T fromIndex(final int index) {
    return inverted.get(index);
  }

  @Override public synchronized List<T> getInverted() {
    return new ArrayList<T>(inverted);
  }

  @Override public TObjectIntHashMap<T> getMap() {
    return new TObjectIntHashMap<T>(map);
  }

  @Override public synchronized T remove(final int index) {
    final T item = inverted.remove(index);
    final int from = map.remove(item) - 1;
    final int size = map.size();
    for(int i = from; i < size; i++)
      map.adjustValue(inverted.get(i), -1);
    return item;
  }

  @Override public synchronized int remove(@NotNull final T element) {
    final int index = map.remove(element) - 1;
    inverted.remove(index);
    final int size = map.size();
    for(int i = index; i < size; i++)
      map.adjustValue(inverted.get(i), -1);
    return index;
  }

  @Override public synchronized int size() {
    if(map.size() != inverted.size())
      throw new RuntimeException("Map size != Inverted size");
    return inverted.size();
  }

  @Override public int toIndex(@NotNull final T element) {
    final int index = map.get(element);
    if(index > 0)
      return index - 1;
    return add(element);
  }

  @Override public synchronized void clear() {
    map.clear();
    inverted.clear();
  }

  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();
    final int size = map.size();
    for(int i = 0; i < size; i++)
      stringBuilder.append(i).append("\t<-->\t").append(inverted.get(i)).append("\n");
    return stringBuilder.toString();
  }

}
