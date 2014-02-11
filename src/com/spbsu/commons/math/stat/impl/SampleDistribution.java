package com.spbsu.commons.math.stat.impl;

import com.spbsu.commons.math.stat.Distribution;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @author vp
 */
public class SampleDistribution<T> implements Distribution<T> {
  protected TObjectIntHashMap<T> samples;
  protected double totalCount;

  public SampleDistribution() {
    samples = new TObjectIntHashMap<T>();
  }

  public void update(final T observation) {
    samples.adjustOrPutValue(observation, 1, 1);
    totalCount++;
  }

  @Override
  public double getProbability(final T observation) {
    return samples.get(observation) / totalCount;
  }

  @Override
  public Object[] getUniversum() {
    return samples.keys();
  }
}
