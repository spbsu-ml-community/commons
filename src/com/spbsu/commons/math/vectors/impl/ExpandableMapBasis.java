package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.GenericBasis;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * ksen | 22:59 26.02.2013 | commons
 */

public class ExpandableMapBasis<T> implements GenericBasis<T> {

  // Basis map for key -> index mapping.
  private TObjectIntHashMap<T> map;
  // Basis list for index -> key mapping.
  private ArrayList<T> index;
  // Default capacity for ArrayList and HashMap.
  private static final int DEFAULT_CAPACITY = 10;

  public ExpandableMapBasis(){
    map = new TObjectIntHashMap<T>(DEFAULT_CAPACITY);
    index = new ArrayList<T>(DEFAULT_CAPACITY);
  }

  public ExpandableMapBasis(int capacity) {
    map = new TObjectIntHashMap<T>(capacity);
    index = new ArrayList<T>(capacity);
  }

  public ExpandableMapBasis(T[] basis) {
    int size = basis.length;
    map = new TObjectIntHashMap<T>(size);
    index = new ArrayList<T>(size);
    for(int i = 0; i < size; i++){
      index.add(basis[i]);
      map.put(basis[i], i);
    }
  }

  public ExpandableMapBasis(GenericBasis<T> basis){
    int size = basis.size();
    T temp;
    map = new TObjectIntHashMap<T>(size);
    index = new ArrayList<T>(size);
    for(int i = 0; i < size; i++){
      temp = basis.fromIndex(i);
      index.add(temp);
      map.put(temp, i);
    }
  }

  // Get key from basis by index.
  @Override
  public T fromIndex(int index) {
    return this.index.get(index);
  }

  // Get index from basis by key. If kye doesn't contain in this basis,
  // then it's put him into basis.
  @Override
  public int toIndex(@NotNull T key) {
    int size;
    if(map.containsKey(key))
      return map.get(key);
    else{
      size = index.size();
      map.put(key, size);
      index.add(key);
      return size;
    }
  }

  // Basis size.
  @Override
  public int size() {
    return index.size();
  }

  public ArrayList<T> getBasis(){
    return index;
  }

  public boolean containKey(@NotNull T key) {
    return map.containsKey(key);
  }

}
