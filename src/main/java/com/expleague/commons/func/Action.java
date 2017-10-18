package com.expleague.commons.func;

/**
 * User: terry
 * Date: 17.10.2008
 */
public interface Action<T> {
  void invoke(T t);
}
