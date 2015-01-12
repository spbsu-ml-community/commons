package com.spbsu.commons.util.cache.impl;

import com.spbsu.commons.func.Computable;
import com.spbsu.commons.util.Pair;
import com.spbsu.commons.util.cache.Cache;
import com.spbsu.commons.util.cache.CacheStrategy;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Igor Kuralenok
 * Date: 31.08.2006
 */
public class FixedSizeCache<K, V> implements Cache<K, V> {
  private final Map<K, CacheSlot<K, V>> accessMap;
  private final Pair<K, V>[] cache;
  private final ReferenceQueue<V> queue = new ReferenceQueue<V>();
  private final CacheStrategy strategy;

  public FixedSizeCache(final int size, final CacheStrategy.Type strategyType) {
    accessMap = new HashMap<>();
    strategy = strategyType.newInstance(size);
    //noinspection unchecked
    cache = (Pair<K, V>[]) new Pair[size];
  }

  @Override
  public V put(final K key, final V value) {
    if (value == null) return value;
    final CacheSlot<K, V> slot = accessMap.get(key);
    return slot != null ? putEntryInSlot(key, value, slot) : putNewEntry(key, value);
  }

  private V putEntryInSlot(final K key, final V value, final @NotNull CacheSlot<K, V> slot) {
    return putEntryInSlot(key, value, slot, alterCacheStrategy(key, cache[slot.position]));
  }

  private V putEntryInSlot(final K key, final V value, final @NotNull CacheSlot<K, V> slot, final boolean alterCacheStrategy) {
    if (slot.reference != null) slot.reference.clearKey();
    slot.reference = new MyWeakReference<K, V>(key, value, queue);
    cache[slot.position] = Pair.create(key, value);
    if (alterCacheStrategy) {
      strategy.registerAccess(slot.position);
    }
    return value;
  }

  private V putNewEntry(final K key, final V value) {
    final int position = strategy.getStorePosition();
    final MyWeakReference<K, V> reference = new MyWeakReference<K, V>(key, value, queue);
    final CacheSlot<K, V> old = accessMap.put(key, new CacheSlot<K, V>(position, reference));
    if (old != null && old.reference != null) old.reference.clearKey();
    if (cache[position] != null) {
      processReferenceQueue();
    }
    cache[position] = Pair.create(key, value);
    strategy.registerAccess(position);
    return value;
  }

  @Override
  public V get(final K key) {
    return get(key, null);
  }

  private int accessIndex = 0;

  @Override
  public V get(final K key, final Computable<K, V> wayToGet) {
    try {
      V result = null;
      final CacheSlot<K, V> slot = accessMap.get(key);
      boolean alterCacheStrategy = true; // whatever
      if (slot != null) {
        result = slot.reference.get();
        final int position = slot.position;
        final Pair<K, V> atPosition = cache[position];
        alterCacheStrategy = alterCacheStrategy(key, atPosition);
        if (alterCacheStrategy) {
          strategy.registerAccess(position);
        }
      }

      if (result == null) {
        if (wayToGet != null) {
          result = wayToGet.compute(key);
        }
        if (result != null) {
          strategy.registerCacheMiss();
          if (slot != null) putEntryInSlot(key, result, slot, alterCacheStrategy);
          else putNewEntry(key, result);
        }
      }
      return result;
    }
    finally {
      if (accessIndex++ % 10000 == 0) processReferenceQueue();
    }
  }

  @Override
  public void flush() {
    for (int i = 0; i < cache.length; i++) {
      cache[i] = null;
    }
    strategy.clear();
  }

  private synchronized void processReferenceQueue() {
    Reference<? extends V> ref;
    while ((ref = queue.poll()) != null) {
      final MyWeakReference<K, V> reference = (MyWeakReference<K, V>) ref;
      final K key = reference.key;
      if (key != null) {
        final CacheSlot<K, V> old = accessMap.remove(key);
        if (old != null && old.reference != null) old.reference.clearKey();
      }
      reference.clearKey();
    }
  }

  public boolean checkEqualSizes() {
    return accessMap.size() == cache.length;
  }

  public CacheStrategy getStrategy() {
    return strategy;
  }

  @Override
  public void clear(final K key) {
    final CacheSlot<K, V> slot = accessMap.remove(key);
    if (slot != null) {
      final int position = slot.position;
      if (slot.reference != null) slot.reference.clearKey();
      final Pair<K, V> atPosition = cache[position];
      if (alterCacheStrategy(key, atPosition)) {
        cache[position] = null;
        strategy.removePosition(position);
      }
    }
  }

  private boolean alterCacheStrategy(final K key, final Pair<K, V> atPosition) {
    return atPosition != null && (atPosition.getFirst() == key || atPosition.getFirst().equals(key));
  }

  @Override
  public void clear() {
    accessMap.clear();
    flush();
  }

  private static class CacheSlot<K, V> {
    int position;
    MyWeakReference<K, V> reference;

    public CacheSlot(final int position, final MyWeakReference<K, V> reference) {
      this.position = position;
      this.reference = reference;
    }
  }

  private static class MyWeakReference<T, R> extends WeakReference<R> {
    private T key;

    public MyWeakReference(final T key, final R referent, final ReferenceQueue<? super R> q) {
      super(referent, q);
      this.key = key;
    }

    public void clearKey() {
      key = null;
    }
  }
}
