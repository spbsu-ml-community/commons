package com.expleague.commons.math.metrics.impl;

import com.expleague.commons.math.metrics.Metric;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;

/**
 * User: qdeee
 * Date: 21.05.14
 *
 * Only for binary vectors!
 */

public class HammingMetric implements Metric<Vec> {
  @Override
  public double distance(final Vec v, final Vec w) {
    return 0.5 * (v.dim() - VecTools.multiply(v, w));
  }
}
