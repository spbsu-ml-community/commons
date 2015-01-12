package com.spbsu.commons.filters;

/**
 * User: dunisher
 * Date: 07.06.2007
 * Time: 23:26:12
 */
public class OrFilter<T> implements Filter<T> {
  private final Filter<T> firstFilter;
  private final Filter<T> secondFilter;

  public OrFilter(final Filter<T> firstFilter, final Filter<T> secondFilter) {
    this.firstFilter = firstFilter;
    this.secondFilter = secondFilter;
  }

  public boolean accept(final T t) {
    return firstFilter.accept(t) || secondFilter.accept(t);
  }

  public Filter<T> getFirstFilter() {
    return firstFilter;
  }

  public Filter<T> getSecondFilter() {
    return secondFilter;
  }
}
