package com.spbsu.commons.math.vectors;

import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * User: solar
 * Date: 20.01.2010
 * Time: 19:50:12
 */
public class MapBasis<T> implements GenericBasis<T> {
  private final TObjectIntHashMap<T> map = new TObjectIntHashMap<T>();
  private final ArrayList<T> inverted = new ArrayList<T>();

  @Override
  public synchronized T fromIndex(int index) {
    return inverted.get(index - 1);
  }

  @Override
  public synchronized int toIndex(T key) {
    int v = map.get(key);
    if (v == 0) {
      v = inverted.size() + 1;
      inverted.add(key);
      map.put(key, v);
    }
    return v;
  }

  @Override
  public synchronized int size() {
    return inverted.size();
  }

  public List<T> getInverted() {
    return inverted;
  }
}
