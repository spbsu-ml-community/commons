package com.spbsu.commons.math.stat.impl;

import com.spbsu.commons.math.stat.Histogram;

/**
 * @author vp
 */
public class ArrayHistogram implements Histogram {
  protected final long[] bins;

  public ArrayHistogram(final int binCount) {
    this.bins = new long[binCount];
  }

  public int getBinCount() {
    return bins.length;
  }

  public long getValue(final int bin) {
    return bins[bin];
  }

  public void addToBin(final int bin, final long value) {
    bins[bin] += value;
  }

  public double getMean() {
    double sum = 0;
    for (final long bin : bins) {
      sum += bin;
    }
    return sum / bins.length;
  }

  @Override
  public double getMean(final int startBin, final int endBin) {
    double sum = 0;
    for (int i = startBin; i <= endBin; i++) {
      sum += bins[i];
    }
    return sum / (endBin - startBin + 1);
  }
}
