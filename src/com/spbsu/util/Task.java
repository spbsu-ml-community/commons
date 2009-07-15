package com.spbsu.util;

/**
 * User: solar
 * Date: 10.06.2007
 */
public interface Task<T> {
  void start(T param);

  void setCompleted();
  boolean isCompleted();
}
