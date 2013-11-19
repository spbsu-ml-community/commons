package com.spbsu.commons.func;

/**
 * User: solar
 * Date: 12.11.13
 * Time: 15:45
 */
public interface IntCombinator<T extends IntCombinator> {
  T append(int index);
  T append(T other);
}
