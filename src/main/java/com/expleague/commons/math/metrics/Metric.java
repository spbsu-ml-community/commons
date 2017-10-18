package com.expleague.commons.math.metrics;

/**
 * @author vp
 */
public interface Metric<T> {
  double distance(final T v, final T w);
}
