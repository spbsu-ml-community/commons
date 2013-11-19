package com.spbsu.commons.func;

/**
 * User: solar
 * Date: 12.11.13
 * Time: 15:45
 */
public interface AdditiveGator<T extends AdditiveGator> extends IntCombinator<T>{
  T append(int index);
  T append(T other);
  T remove(int index);
  T remove(T other);
}
