package com.spbsu.commons.filters;

/**
 * User: dunisher
 * Date: 07.06.2007
 * Time: 23:27:52
 */
public class TrueFilter<T> implements Filter<T> {
  public boolean accept(T t) {
    return true;
  }

  public boolean accept(FilterVisitor visitor) {
    visitor.visit(this);
    return true;
  }

  public static <T> Filter<T> create() {
    return new TrueFilter<T>();
  }
}