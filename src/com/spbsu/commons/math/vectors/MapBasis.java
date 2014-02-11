package com.spbsu.commons.math.vectors;


import gnu.trove.map.hash.TObjectIntHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/* Created with IntelliJ IDEA.
 * ksen | 18:34 26.02.2013 | commons
 */

//TODO(ksen): equals and hashCode, bulk add/remove
// Cann't contain NULL. Each element in basis is unique.
public class MapBasis<T> implements GenericBasis<T> {

  private final TObjectIntHashMap<T> map;
  private final List<T> inverted;

  public MapBasis() {
    map = new TObjectIntHashMap<T>();
    inverted = new ArrayList<T>();
  }

  public MapBasis(int capacity) {
    map = new TObjectIntHashMap<T>(capacity);
    inverted = new ArrayList<T>(capacity);
  }

  public MapBasis(T[] basis) {
    this(basis.length);
    int size = basis.length;
    for (int i = 0; i < size; i++) {
      add(basis[i]);
    }
    if(map.size() != basis.length)
      throw new RuntimeException(new IllegalArgumentException("Basis must contain unique keys."));
  }

  public MapBasis(Collection<T> basis) {
    this(basis.size());
    int size = basis.size();
    Iterator<T> iterator = basis.iterator();
    for (int i = 0; i < size; i++) {
      add(iterator.next());
    }
    if(map.size() != basis.size())
      throw new RuntimeException(new IllegalArgumentException("Basis must contain unique keys."));
  }

  public MapBasis(GenericBasis<T> basis) {
    this(basis.size());
    int size = basis.size();
    T item;
    for (int i = 0; i < size; i++) {
      add(basis.fromIndex(i));
    }
  }

  @Override public synchronized int add(@NotNull T element) {
    if(map.containsKey(element))
      return map.get(element) - 1;
    else {
      map.put(element, inverted.size() + 1);
      inverted.add(element);
      return inverted.size() - 1;
    }
  }

  @Override public synchronized T fromIndex(int index) {
    return inverted.get(index);
  }

  @Override public synchronized List<T> getInverted() {
    return new ArrayList<T>(inverted);
  }

  @Override public TObjectIntHashMap<T> getMap() {
    return new TObjectIntHashMap<T>(map);
  }

  @Override public synchronized T remove(int index) {
    T item = inverted.remove(index);
    int from = map.remove(item) - 1;
    int size = map.size();
    for(int i = from; i < size; i++)
      map.adjustValue(inverted.get(i), -1);
    return item;
  }

  @Override public synchronized int remove(@NotNull T element) {
    int index = map.remove(element) - 1;
    inverted.remove(index);
    int size = map.size();
    for(int i = index; i < size; i++)
      map.adjustValue(inverted.get(i), -1);
    return index;
  }

  @Override public synchronized int size() {
    if(map.size() != inverted.size())
      throw new RuntimeException("Map size != Inverted size");
    return inverted.size();
  }

  @Override public int toIndex(@NotNull T element) {
    final int index = map.get(element);
    if(index > 0)
      return index - 1;
    return add(element);
  }

  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    int size = map.size();
    for(int i = 0; i < size; i++)
      stringBuilder.append(i).append("\t<-->\t").append(inverted.get(i)).append("\n");
    return stringBuilder.toString();
  }

}
