package com.spbsu.commons.func;

/**
 * User: amosov-f
 * Date: 22.05.15
 * Time: 16:33
 */
public interface BiAction<A, B> {
  void invoke(A a, B b);
}
