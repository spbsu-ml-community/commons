package com.spbsu.commons.math.stat.impl;

import com.spbsu.commons.math.stat.Histogram;
import com.spbsu.commons.util.time.TimeFrame;

/**
 * @author vp
 */
public class FrameHistogram implements Histogram {
  public static FrameHistogram createByResolution(final TimeFrame frame, final long resolution) {
    return new FrameHistogram(frame, resolution, false);
  }

  public static FrameHistogram createByBinCount(final TimeFrame frame, final int binCount) {
    return new FrameHistogram(frame, binCount, false);
  }

  public static FrameHistogram createSparseByResolution(final TimeFrame frame, final long resolution) {
    return new FrameHistogram(frame, resolution, true);
  }

  public static FrameHistogram createSparseByBinCount(final TimeFrame frame, final int binCount) {
    return new FrameHistogram(frame, binCount, true);
  }

  private final Histogram impl;
  private final TimeFrame frame;
  private final long resolution;

  protected FrameHistogram(final TimeFrame frame, final long resolution, final boolean sparse) {
    this.frame = frame;
    this.resolution = resolution;
    final int binCount = (int) Math.ceil((frame.durationMs()) / (double) resolution);
    this.impl = sparse ? new SparseHistogram(binCount) : new ArrayHistogram(binCount);
  }

  protected FrameHistogram(final TimeFrame frame, final int binCount, final boolean sparse) {
    this.frame = frame;
    this.resolution = frame.durationMs() / binCount;
    this.impl = sparse ? new SparseHistogram(binCount) : new ArrayHistogram(binCount);
  }

  public TimeFrame getFrame() {
    return frame;
  }

  public long getResolution() {
    return resolution;
  }

  public void update(final long timestamp, final long value) {
    if (timestamp == frame.getEndTime()) addToBin(getBinCount() - 1, value);
    else addToBin((int) ((timestamp - frame.getStartTime()) / resolution), value);
  }

  @Override
  public void addToBin(final int bin, final long value) {
    impl.addToBin(bin, value);
  }

  @Override
  public int getBinCount() {
    return impl.getBinCount();
  }

  @Override
  public double getMean() {
    return impl.getMean();
  }

  @Override
  public double getMean(int startBin, int endBin) {
    return impl.getMean(startBin, endBin);
  }

  @Override
  public long getValue(final int bin) {
    return impl.getValue(bin);
  }
}
