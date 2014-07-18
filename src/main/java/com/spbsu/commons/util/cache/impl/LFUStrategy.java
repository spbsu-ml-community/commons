package com.spbsu.commons.util.cache.impl;

import java.util.Arrays;


import com.spbsu.commons.util.ArrayTools;
import com.spbsu.commons.util.cache.CacheStrategy;

/**
 * User: Igor Kuralenok
 * Date: 31.08.2006
 */
public class LFUStrategy implements CacheStrategy {
  private int misses;
  private int access;
  private double[] usages;

  public LFUStrategy(int size) {
    init(size);
  }

  private void init(int size) {
    usages = new double[size];
  }

  public int getStorePosition() {
    final int min = ArrayTools.min(usages);
    usages[min] = 1;
    return min;
  }

  public void registerAccess(int position) {
    usages[position] = usages[position] + 1;
    access++;
  }

  public void removePosition(int position) {
    usages[position] = 0;
  }

  public void registerCacheMiss() {
    misses++;
  }

  public int getAccessCount() {
    return access;
  }

  public int getCacheMisses() {
    return misses;
  }

  public void clear() {
    Arrays.fill(usages, 0.);
    misses = 0;
    access = 0;
  }

}
