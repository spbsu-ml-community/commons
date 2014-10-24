package com.spbsu.commons.filters;

/**
 * User: dunisher
 * Date: 07.06.2007
 * Time: 23:23:54
 */
public class AndFilter<T> implements Filter<T> {
  private Filter<T>[] filters;

  public AndFilter(Filter... filters) {
    //noinspection unchecked
    this.filters = (Filter<T>[])filters;
  }

  public boolean accept(T t) {
    for(int i = 0; i < filters.length; i++) {
      if (!filters[i].accept(t))
        return false;
    }
    return true;
  }

  public Filter<T>[] filters() {
    return filters;
  }
}
