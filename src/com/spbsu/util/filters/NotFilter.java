package com.spbsu.util.filters;

/**
 * User: dunisher
 * Date: 07.06.2007
 */
public class NotFilter<T> implements Filter<T> {
  private Filter<T> filter;

  public NotFilter(Filter<T> filter) {
    this.filter = filter;
  }

  public boolean accept(T t) {
    return !filter.accept(t);
  }

  public boolean accept(FilterVisitor visitor) {
    visitor.visit(this);
    return true;
  }

  public Filter<T> getChildFilter() {
    return filter;
  }
}

