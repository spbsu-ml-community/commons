package com.spbsu.commons.math.vectors;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 18:25:58
 */
public interface GenericBasis<T> extends Basis{
  T fromIndex(int index);
  int toIndex(T key);
  int size();
}
