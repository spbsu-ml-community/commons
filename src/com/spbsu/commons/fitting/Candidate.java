package com.spbsu.commons.fitting;

import com.spbsu.commons.util.Pair;

/**
 * @author vp
 */
public interface Candidate<T extends Number> {
  public void registerMetric(final String metricName, final double value);
  public Pair<Factor<T>, T>[] getFactorValues();
  public Pair<String, Double>[] getMetrics();
}
