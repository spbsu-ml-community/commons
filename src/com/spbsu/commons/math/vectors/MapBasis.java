package com.spbsu.commons.math.vectors;

import gnu.trove.TObjectIntHashMap;
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
  private final ArrayList<T> inverted;

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
      inverted.add(basis[i]);
      map.put(basis[i], i);
    }
    if(map.size() != basis.length)
      throw new RuntimeException(new IllegalArgumentException("Basis must contain unique keys."));
  }

  public MapBasis(Collection<T> basis) {
    this(basis.size());
    int size = basis.size();
    Iterator<T> iterator = basis.iterator();
    T item;
    for (int i = 0; i < size; i++) {
      item = iterator.next();
      inverted.add(item);
      map.put(item, i );
    }
    if(map.size() != basis.size())
      throw new RuntimeException(new IllegalArgumentException("Basis must contain unique keys."));
  }

  public MapBasis(GenericBasis<T> basis) {
    this(basis.size());
    int size = basis.size();
    T item;
    for (int i = 0; i < size; i++) {
      item = basis.fromIndex(i);
      inverted.add(item);
      map.put(item, i);
    }
  }

  @Override public synchronized int add(@NotNull T element) {
    if(map.containsKey(element))
      return map.get(element);
    else {
      map.put(element, inverted.size());
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
    return map.clone();
  }

  @Override public synchronized T remove(int index) {
    T item = inverted.remove(index);
    int from = map.remove(item);
    int size = map.size();
    for(int i = from; i < size; i++)
      map.adjustValue(inverted.get(i), -1);
    return item;
  }

  @Override public synchronized int remove(@NotNull T element) {
    int index = map.remove(element);
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

  @Override public synchronized int toIndex(@NotNull T element) {
    if(map.containsKey(element))
      return map.get(element);
    else
      return -1;
  }

  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    int size = map.size();
    for(int i = 0; i < size; i++)
      stringBuilder.append(i).append("\t<-->\t").append(inverted.get(i)).append("\n");
    return stringBuilder.toString();
  }

}
