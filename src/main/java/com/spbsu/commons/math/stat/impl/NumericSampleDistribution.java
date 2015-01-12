package com.spbsu.commons.math.stat.impl;

import com.spbsu.commons.math.stat.NumericDistribution;

/**
 * @author vp
 */
public class NumericSampleDistribution<T extends Number> extends SampleDistribution<T> implements NumericDistribution<T> {
  private double valueSum;
  private double valueSqSum;
  private double minValue;
  private double maxValue;

  public NumericSampleDistribution() {
    minValue = Double.MAX_VALUE;
    maxValue = Double.MIN_VALUE;
  }

  @Override
  public void update(final T observation) {
    super.update(observation);
    final double v = observation.doubleValue();
    valueSum += v;
    valueSqSum += v * v;
    minValue = Math.min(minValue, v);
    maxValue = Math.max(maxValue, v);
  }

  @Override
  public double getMean() {
    return valueSum / totalCount;
  }

  @Override
  public double getVariance() {
    return totalCount <= 1 ? 0 : (valueSqSum - valueSum * valueSum / totalCount) / (totalCount - 1);
  }

  @Override
  public double getStandardDeviation() {
    return Math.sqrt(getVariance());
  }

  @Override
  public double getMax() {
    return maxValue;
  }

  @Override
  public double getMin() {
    return minValue;
  }
}
