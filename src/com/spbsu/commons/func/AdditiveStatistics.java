package com.spbsu.commons.func;

/**
 * User: solar
 * Date: 12.11.13
 * Time: 15:45
 */
public interface AdditiveStatistics<T extends AdditiveStatistics> {
  T append(int index, int times);
  T append(T other);
  T remove(int index, int times);
  T remove(T other);
}
