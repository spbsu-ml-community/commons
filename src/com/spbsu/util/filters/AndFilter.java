package com.spbsu.util.filters;

/**
 * Created by IntelliJ IDEA.
 * User: dunisher
 * Date: 07.06.2007
 * Time: 23:23:54
 * To change this template use File | Settings | File Templates.
 */
public class AndFilter<T> implements Filter<T> {
  private Filter<T> firstFilter;
  private Filter<T> secondFilter;

  public AndFilter(Filter<T> firstFilter, Filter<T> secondFilter) {
    this.firstFilter = firstFilter;
    this.secondFilter = secondFilter;
  }

  public boolean accept(T t) {
    return firstFilter.accept(t) && secondFilter.accept(t);
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
