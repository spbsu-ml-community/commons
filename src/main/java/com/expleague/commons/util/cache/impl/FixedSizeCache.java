package com.expleague.commons.util.cache.impl;

import com.expleague.commons.util.Pair;
import com.expleague.commons.util.cache.Cache;
import com.expleague.commons.util.cache.CacheStrategy;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * User: Igor Kuralenok
 * Date: 31.08.2006
 */
public class FixedSizeCache<K, V> implements Cache<K, V> {
  private final Map<K, CacheSlot<K, V>> accessMap;
  private final Pair<K, V>[] cache;
  private final ReferenceQueue<V> queue = new ReferenceQueue<>();
  private final CacheStrategy strategy;

  public FixedSizeCache(final int size, final CacheStrategy.Type strategyType) {
    accessMap = new HashMap<>();
    strategy = strategyType.newInstance(size);
    //noinspection unchecked
    cache = (Pair<K, V>[]) new Pair[size];
  }

  @Override
  public synchronized V put(final K key, final V value) {
    if (value == null) return null;
    final CacheSlot<K, V> slot = accessMap.get(key);
    return slot != null ? putEntryInSlot(key, value, slot) : putNewEntry(key, value);
  }

  private synchronized V putEntryInSlot(final K key, final V value, final @NotNull CacheSlot<K, V> slot) {
    return putEntryInSlot(key, value, slot, alterCacheStrategy(key, cache[slot.position]));
  }

  private synchronized V putEntryInSlot(final K key, final V value, final @NotNull CacheSlot<K, V> slot, final boolean alterCacheStrategy) {
    if (slot.reference != null) slot.reference.clearKey();
    slot.reference = new MyWeakReference<>(key, value, queue);
    cache[slot.position] = Pair.create(key, value);
    if (alterCacheStrategy) {
      strategy.registerAccess(slot.position);
    }
    return value;
  }

  private synchronized V putNewEntry(final K key, final V value) {
    final int position = strategy.getStorePosition();
    final MyWeakReference<K, V> reference = new MyWeakReference<>(key, value, queue);
    final CacheSlot<K, V> old = accessMap.put(key, new CacheSlot<>(position, reference));
    if (old != null && old.reference != null) old.reference.clearKey();
    if (cache[position] != null) {
      processReferenceQueue();
    }
    cache[position] = Pair.create(key, value);
    strategy.registerAccess(position);
    return value;
  }

  @Override
  public synchronized V get(final K key) {
    return get(key, null);
  }

  private int accessIndex = 0;

  @Override
  public V get(final K key, final Function<K, V> wayToGet) {
    try {
      V result = null;
      final CacheSlot<K, V> slot;
      boolean alterCacheStrategy; // whatever
      synchronized (this) {
        slot = accessMap.get(key);
        if (slot != null) {
          result = slot.reference.get();
          final int position = slot.position;
          final Pair<K, V> atPosition = cache[position];
          alterCacheStrategy = alterCacheStrategy(key, atPosition);
          if (alterCacheStrategy) {
            strategy.registerAccess(position);
          }
        }
      }

      if (result == null) {
        if (wayToGet != null) {
          result = wayToGet.apply(key);
        }
        if (result != null) {
          strategy.registerCacheMiss();
          put(key, result);
//          if (slot != null) putEntryInSlot(key, result, slot, alterCacheStrategy);
//          else putNewEntry(key, result);
        }
      }
      return result;
    }
    finally {
      if (accessIndex++ % 10000 == 0) processReferenceQueue();
    }
  }

  @Override
  public synchronized void flush() {
    for (int i = 0; i < cache.length; i++) {
      cache[i] = null;
    }
    strategy.clear();
  }

  private synchronized void processReferenceQueue() {
    Reference<? extends V> ref;
    while ((ref = queue.poll()) != null) {
      //noinspection unchecked
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
  public synchronized void clear(final K key) {
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
  public synchronized void clear() {
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
