package com.spbsu.commons.filters;

/**
 * User: solar
 * Date: 02.06.2007
 * Time: 13:41:10
 */
public interface Filter<T> {
  boolean accept(T t);
}
