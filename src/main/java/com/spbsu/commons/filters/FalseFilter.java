package com.spbsu.commons.filters;

/**
 * User: terry
 * Date: 16.11.2007
 * Time: 14:18:51
 */
public class FalseFilter<T> implements Filter<T> {
  public boolean accept(T t) {
    return false;
  }

  public static <T> Filter<T> create() {
    return new FalseFilter<T>();
  }
}
