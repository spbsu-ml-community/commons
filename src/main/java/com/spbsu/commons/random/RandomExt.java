package com.spbsu.commons.random;

import java.util.Random;

import static java.lang.Math.exp;

/**
 * Created by IntelliJ IDEA.
 * User: solar
 * Date: 19.01.12
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
public class RandomExt extends Random {
  Random basedOn;
  public RandomExt(Random basedOn) {
    this.basedOn = basedOn;
  }

  /** Standard Knuth implementation. Use normal approximation for frequencies > 25 */
  public int nextPoisson(double meanFreq) {
    if (meanFreq > 25) {
      final double val = nextNormal(meanFreq, Math.sqrt(meanFreq));
      if (val < 0)
        return nextPoisson(meanFreq);
      return (int) val + (val - (int)val >= 0.5 ? 1 : 0);
    }
    double L = exp(-meanFreq);
    int k = 0;
    double p = 1;
    do {
      k++;
      p *= nextDouble();
    }
    while(p > L);
    return k - 1;
  }

  private double nextNormal(double meanFreq, double stddev) {
    return nextGaussian() * stddev + meanFreq;
  }
}