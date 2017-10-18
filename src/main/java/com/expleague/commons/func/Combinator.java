package com.expleague.commons.func;

/**
 * User: solar
 * Date: 11.11.15
 * Time: 14:46
 */
public interface Combinator<T> {
  long age();
  Combinator combine(T x, long time);
}
