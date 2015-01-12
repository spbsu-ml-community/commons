package com.spbsu.commons.filters;

/**
 * User: dunisher
 * Date: 07.06.2007
 * Time: 23:23:54
 */
public class AndFilter<T> implements Filter<T> {
  private final Filter<T>[] filters;
  private String failureReason = null;

  public AndFilter(final Filter... filters) {
    //noinspection unchecked
    this.filters = (Filter<T>[])filters;
  }

  @Override
  public boolean accept(final T t) {
    for(int i = 0; i < filters.length; i++) {
      if (!filters[i].accept(t)) {
        // doing this not to break the existent API
        if (filters[i] instanceof ExplainableFilter) {
          failureReason = ((ExplainableFilter) filters[i]).explain();
        }
        return false;
      }
    }
    return true;
  }

  public Filter<T>[] filters() {
    return filters;
  }

  public String getFailureReason() {
    return failureReason;
  }
}
