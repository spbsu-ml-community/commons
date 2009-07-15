package com.spbsu.util.filters;

/**
 * User: solar
 * Date: 02.06.2007
 */
public interface Filter<T> {
  TrueFilter TRUE_FILTER = new TrueFilter();

  boolean accept(T t);
  boolean accept(FilterVisitor visitor);
}
