package com.spbsu.util.filters;

/**
 * Created by IntelliJ IDEA.
 * User: dunisher
 * Date: 07.06.2007
 * Time: 23:29:31
 * To change this template use File | Settings | File Templates.
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

