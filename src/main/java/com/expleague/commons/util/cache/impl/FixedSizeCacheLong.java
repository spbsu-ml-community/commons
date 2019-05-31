package com.expleague.commons.util.cache.impl;

import com.expleague.commons.util.cache.Cache;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public class FixedSizeCacheLong<V> implements Cache<Long, V> {

  private final int size;
  private volatile ConcurrentHashMap<Long, V> curMap;
  private volatile ConcurrentHashMap<Long, V> newMap;

  public FixedSizeCacheLong(int size) {
    this.size = size;
    curMap = new ConcurrentHashMap<>(size);
    newMap = new ConcurrentHashMap<>(size);
  }

  @Override
  public V put(Long key, V value) {
    if (value == null) {
      return null;
    }
    if (newMap.size() >= size) {
      synchronized (this) {
        if (newMap.size() >= size) {
          curMap = newMap;
          newMap = new ConcurrentHashMap<>(size);
        }
      }
    }
    return newMap.put(key, value);
  }

  @Nullable
  @Override
  public V get(Long key) {
    final V value = newMap.get(key);
    return value == null ? curMap.get(key) : value;
  }

  @Override
  public void flush() {

  }

  @Override
  public synchronized void clear() {
    curMap.clear();
    newMap.clear();
  }

  @Override
  public V get(Long key, Function<Long, V> wayToGet) {
    final V value = get(key);
    if (value == null) {
      final V newValue = wayToGet.apply(key);
      put(key, newValue);
      return newValue;
    } else {
      return value;
    }
  }

  @Override
  public void clear(Long key) {
    curMap.remove(key);
    newMap.remove(key);
  }
}
