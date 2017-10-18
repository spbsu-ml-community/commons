package com.expleague.commons.math.vectors;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.List;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 18:25:58
 */
public interface GenericBasis<T> extends Basis {

  int add(T element);

  T fromIndex(int index);

  List<T> getInverted();

  TObjectIntHashMap<T> getMap();

  T remove(int index);

  int remove(T element);

  @Override
  int size();

  int toIndex(T element);

  void clear();
}
