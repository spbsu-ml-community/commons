package com.spbsu.commons.func;

import com.spbsu.commons.math.vectors.Vec;

/**
 * User: solar
 * Date: 12.11.13
 * Time: 15:45
 */
public interface AdditiveStatistics {
  AdditiveStatistics append(int index, int times);

  AdditiveStatistics append(int index, double weight, int times);

  AdditiveStatistics append(int index, double weight);


  AdditiveStatistics append(AdditiveStatistics other);

  AdditiveStatistics remove(int index, int times);

  AdditiveStatistics remove(AdditiveStatistics other);

  AdditiveStatistics remove(int index, double weight, int times);

  AdditiveStatistics remove(int index, double weight);

  Vec getTargets();
}


