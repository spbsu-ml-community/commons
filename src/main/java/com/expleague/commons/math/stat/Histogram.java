package com.expleague.commons.math.stat;

/**
 * @author vp
 */
public interface Histogram {
  public int getBinCount();
  public long getValue(final int bin);
  public void addToBin(final int bin, final long value);
  public double getMean();
  public double getMean(final int startBin, final int endBin);
}
