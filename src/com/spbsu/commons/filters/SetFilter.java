package com.spbsu.commons.filters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: alms
 * Date: 14.04.2009
 * Time: 15:54:18
 */
public class SetFilter<T> extends UnvisitableFilter<T> {

  public static <T> SetFilter<T> create(final Collection<T> filteredCollection) {
    final Set<T> set = new HashSet<T>(filteredCollection);
    return new SetFilter<T>(set);
  }

  private final Set<T> filteredSet;

  public SetFilter(final Set<T> filteredSet) {
    this.filteredSet = filteredSet;
  }

  @Override
  public boolean accept(final T t) {
    return filteredSet.contains(t);
  }
}
