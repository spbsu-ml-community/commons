package com.spbsu.commons.func;

/**
 * User: solar
 * Date: 12.11.13
 * Time: 15:45
 */
public interface AdditiveStatistics {
  AdditiveStatistics append(int index, int times);
  AdditiveStatistics append(AdditiveStatistics other);
  AdditiveStatistics remove(int index, int times);
  AdditiveStatistics remove(AdditiveStatistics other);
}
