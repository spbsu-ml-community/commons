package com.spbsu.commons.math.stat.tools;

import com.spbsu.commons.math.stat.Histogram;

/**
 * @author vp
 */
public abstract class HistogramTools {
  private HistogramTools() {}

  public static double correlation(final Histogram h1, final Histogram h2) {
    final int n = h1.getBinCount();
    if (n != h2.getBinCount()) return Double.MIN_VALUE;

    long sx = 0, sy = 0, sxx = 0, syy = 0, sxy = 0;
    for (int i = 0; i < n; i++) {
      final long xi = h1.getValue(i);
      final long yi = h2.getValue(i);
      sx += xi;
      sy += yi;
      sxx += xi * xi;
      syy += yi * yi;
      sxy += xi * yi;
    }
    return ((double) (n * sxy - sx * sy)) / Math.sqrt((n * sxx - sx * sx) * (n * syy - sy * sy));
  }

}
