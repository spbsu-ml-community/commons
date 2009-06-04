package com.spbsu.util;

/**
 * User: solar
 * Date: 10.06.2007
 * Time: 16:59:08
 */
public interface Task<T> {
  void start(T param);

  void setCompleted();
  boolean isCompleted();
}
