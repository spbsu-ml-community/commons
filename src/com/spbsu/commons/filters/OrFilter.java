package com.spbsu.commons.filters;

/**
 * User: dunisher
 * Date: 07.06.2007
 * Time: 23:26:12
 */
public class OrFilter<T> implements Filter<T> {
  private Filter<T> firstFilter;
  private Filter<T> secondFilter;

  public OrFilter(Filter<T> firstFilter, Filter<T> secondFilter) {
    this.firstFilter = firstFilter;
    this.secondFilter = secondFilter;
  }

  public boolean accept(T t) {
    return firstFilter.accept(t) || secondFilter.accept(t);
  }

  public boolean accept(FilterVisitor visitor) {
    visitor.visit(this);
    return true;
  }

  public Filter<T> getFirstFilter() {
    return firstFilter;
  }

  public Filter<T> getSecondFilter() {
    return secondFilter;
  }
}
