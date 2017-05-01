package com.spbsu.commons.util.cache.impl;

import java.util.Arrays;


import com.spbsu.commons.util.ArrayTools;
import com.spbsu.commons.util.cache.CacheStrategy;

/**
 * User: Igor Kuralenok
 * Date: 31.08.2006
 */
public class LFUStrategy implements CacheStrategy {
  private volatile int misses;
  private volatile int access;
  private double[] usages;

  public LFUStrategy(final int size) {
    init(size);
  }

  private void init(final int size) {
    usages = new double[size];
  }

  @Override
  public int getStorePosition() {
    final int min = ArrayTools.min(usages);
    usages[min] = 1;
    return min;
  }

  @Override
  public void registerAccess(final int position) {
    usages[position] = usages[position] + 1;
    access++;
  }

  @Override
  public void removePosition(final int position) {
    usages[position] = 0;
  }

  @Override
  public void registerCacheMiss() {
    misses++;
  }

  @Override
  public int getAccessCount() {
    return access;
  }

  @Override
  public int getCacheMisses() {
    return misses;
  }

  @Override
  public void clear() {
    Arrays.fill(usages, 0.);
    misses = 0;
    access = 0;
  }

}
