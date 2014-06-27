package com.spbsu.commons.filters;

/**
 * User: solar
 * Date: 25.06.13
 * Time: 10:41
 */
public class ClassFilter<T> implements Filter<T> {
  public final Class<? extends T> clazz;

  public ClassFilter(final Class<? extends T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public boolean accept(final T t) {
    return clazz.isAssignableFrom(t.getClass());
  }

}
