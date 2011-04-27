package com.spbsu.commons.filters;

/**
 * User: terry
 * Date: 16.11.2007
 * Time: 14:06:16
 */
public abstract class UnvisitableFilter<T> implements Filter<T>{

  public final boolean accept(FilterVisitor visitor) {
    throw new UnsupportedClassVersionError("this filter not support visiters");
  }
}
