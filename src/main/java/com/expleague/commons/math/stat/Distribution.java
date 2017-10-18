package com.expleague.commons.math.stat;

/**
 * @author vp
 */
public interface Distribution<T> {
  public double getProbability(final T observation);
  public Object[] getUniversum();
}
