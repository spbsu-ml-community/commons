package com.spbsu.commons.math.metrics;

/**
 * @author vp
 */
public interface Metric<T> {
  double distance(final T v, final T w);
}
