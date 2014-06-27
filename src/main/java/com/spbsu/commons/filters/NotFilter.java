package com.spbsu.commons.filters;

/**
 * User: dunisher
 * Date: 07.06.2007
 * Time: 23:29:31
 */
public class NotFilter<T> implements Filter<T> {
  private Filter<T> filter;

  public NotFilter(Filter<T> filter) {
    this.filter = filter;
  }

  public boolean accept(T t) {
    return !filter.accept(t);
  }

  public Filter<T> getChildFilter() {
    return filter;
  }
}

