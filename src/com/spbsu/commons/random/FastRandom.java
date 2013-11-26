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
public class FastRandom extends Random {
//  private Lock l = new ReentrantLock();
  private volatile long u;
  private volatile long v = 4101842887655102017L;
  private volatile long w = 1;

  public FastRandom() {
    this(System.nanoTime());
  }

  public FastRandom(long seed) {
//    l.lock();
    u = seed ^ v;
    nextLong();
    v = u;
    nextLong();
    w = v;
    nextLong();
//    l.unlock();
  }

  public long nextLong() {
//    l.lock();
//    try {
    long localU = u;
    long localV = v;
    long localW = w;
    
    localU = localU * 2862933555777941757L + 7046029254386353087L;
    localV ^= localV >>> 17;
    localV ^= localV << 31;
    localV ^= localV >>> 8;
    localW = 4294957665L * (localW & 0xffffffff) + (localW >>> 32);
    long x = localU ^ (localU << 21);
    x ^= x >>> 35;
    x ^= x << 4;
    long ret = (x + localV) ^ localW;
    u = localU;
    v = localV;
    w = localW;
    return ret;
//    } finally {
//      l.unlock();
//    }
  }

  protected int next(int bits) {
    return (int) (nextLong() >>> (64-bits));
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