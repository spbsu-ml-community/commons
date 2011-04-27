package com.spbsu.commons.util.cache.impl;

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
    return -1;
  }

  public void registerAccess(int position) {
    usages[position] = 0.99 * usages[position] + 1;
    access++;
  }

  public void removePosition(int position) {
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
    init(usages.length);
    misses = 0;
    access = 0;
  }

}
