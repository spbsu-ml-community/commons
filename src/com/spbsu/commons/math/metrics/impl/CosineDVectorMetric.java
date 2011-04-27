package com.spbsu.commons.math.metrics.impl;

import com.spbsu.commons.math.metrics.Metric;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecTools;

/**
 * User: terry
 * Date: 16.01.2010
 */
public class CosineDVectorMetric implements Metric<Vec> {
  @Override
  public double distance(Vec v, Vec w) {
    return 1 - VecTools.cosine(v, w);
  }
}
