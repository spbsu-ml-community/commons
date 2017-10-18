package com.expleague.commons.math.stat;

/**
 * @author vp
 */
public interface NumericDistribution<T extends Number> extends Distribution<T> {
  double getMean();
  double getVariance();
  double getStandardDeviation();
  double getMax();
  double getMin();
}
