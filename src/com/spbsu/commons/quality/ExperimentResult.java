package com.spbsu.commons.quality;

/**
 * User: selivanov
 * Date: 12.11.2009 : 20:13:50
 */
public class ExperimentResult {
  public int ss = 0;
  public int sf = 0;
  public int fs = 0;

  public double precision() {
    return ss + fs == 0 ? 0. : (1. * ss) / (ss + fs);
  }

  public double recall() {
    return ss + sf == 0 ? 0. : (1. * ss) / (ss + sf);
  }

  public double f1Measure() {
    final double p = precision();
    final double r = recall();
    return p + r == 0 ? 0. : 2. * p * r / (p + r);
  }

  public void macroAdjust(ExperimentResult result) {
    ss += result.ss;
    sf += result.sf;
    fs += result.fs;
  }
}
