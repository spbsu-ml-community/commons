package com.spbsu.util.filters;

/**
 * User: dunisher
 * Date: 07.06.2007
 */
public class TrueFilter<T> implements Filter<T> {
  public boolean accept(T t) {
    return true;
  }

  public boolean accept(FilterVisitor visitor) {
    visitor.visit(this);
    return true;
  }
}