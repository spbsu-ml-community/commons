package com.spbsu.commons.math.stat.impl;

import com.spbsu.commons.math.signals.numeric.IntSignal;
import com.spbsu.commons.math.signals.tools.NumericSignalTools;
import com.spbsu.commons.math.stat.Histogram;

/**
 * @author vp
 */
public class SparseHistogram implements Histogram {
  protected final int binCount;
  protected final IntSignal timestamps;

  public SparseHistogram(final int binCount) {
    this.binCount = binCount;
    this.timestamps = new IntSignal();
  }

  public int getBinCount() {
    return binCount;
  }

  public long getValue(final int bin) {
    return timestamps.ceil(bin) == bin ? timestamps.getNativeValue(timestamps.ceilIndex(bin)) : 0;
  }

  public void addToBin(final int bin, final long value) {
    timestamps.occur(bin, (int)value);
  }

  public double getMean() {
    return ((double) NumericSignalTools.sumValues(timestamps)) / binCount;
  }

  @Override
  public double getMean(final int startBin, final int endBin) {
    if (timestamps.getTimestampCount() == 0) return 0;
    final int ceilIndex = timestamps.ceilIndex(startBin);
    if (ceilIndex == -1) return 0;
    final long ceil = timestamps.getTimestamp(ceilIndex);
    if (ceil > endBin) return 0;

    double sum = 0;
    for (int i = ceilIndex; i <= timestamps.floorIndex(endBin); i++) {
      sum += timestamps.getNativeValue(i);
    }
    return sum / (endBin - startBin + 1);
  }
}