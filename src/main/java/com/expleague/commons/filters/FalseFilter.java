package com.expleague.commons.filters;

/**
 * User: terry
 * Date: 16.11.2007
 * Time: 14:18:51
 */
public class FalseFilter<T> implements Filter<T> {
  @Override
  public boolean accept(final T t) {
    return false;
  }

  public static <T> Filter<T> create() {
    return new FalseFilter<T>();
  }
}
