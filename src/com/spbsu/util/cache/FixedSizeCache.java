package com.spbsu.util.cache;

import com.spbsu.util.Computable;
import com.spbsu.util.Pair;
import com.spbsu.util.cache.impl.LRUStrategy;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: Igor Kuralenok
 * Date: 31.08.2006
 */
public class FixedSizeCache<K, V> implements Cache<K, V>{
  private final Map<K, Pair<Integer, WeakReference<V>>> accessMap;
  private final Map<WeakReference<V>, K> invertedAccessMap;
  private final Pair<K, V>[] cache;
  private final ReferenceQueue<V> queue = new ReferenceQueue<V>();
  private final CacheStrategy strategy;

  public FixedSizeCache(int size, CacheStrategy.Type strategyType) {
    this(size, strategyType, false);
  }

  public FixedSizeCache(int size, CacheStrategy.Type strategyType, boolean concurrency) {
    if (concurrency) {
      accessMap = new ConcurrentHashMap<K, Pair<Integer, WeakReference<V>>>();
      invertedAccessMap = new ConcurrentHashMap<WeakReference<V>, K>();
    } else {
      accessMap = new HashMap<K, Pair<Integer, WeakReference<V>>>();
      invertedAccessMap = new HashMap<WeakReference<V>, K>();
    }
    switch(strategyType){
      case LRU: strategy = new LRUStrategy(size); break;
      default: strategy = null;
    }
    //noinspection unchecked
    cache = (Pair<K, V>[])new Pair[size];
  }

  public V put(K key, V value) {
    if(value == null) return value;
    final int position = strategy.getStorePosition();
    final WeakReference<V> reference = new WeakReference<V>(value, queue);
    accessMap.put(key, Pair.create(position, reference));
    invertedAccessMap.put(reference, key);
    if(cache[position] != null){
      if(cache[position].getSecond() instanceof CacheItem){
        ((CacheItem) cache[position].getSecond()).notifyRemove();
        final Pair<Integer, WeakReference<V>> access = accessMap.remove(cache[position].getFirst());
        invertedAccessMap.remove(access.getSecond());
      }
      processReferenceQueue();
    }
    cache[position] = Pair.create(key, value);
    strategy.registerAccess(position);
    return value;
  }

  public V get(K key){
    return get(key, null);
  }

  private int accessIndex = 0;
  public V get(K key, Computable<K, V> wayToGet){
    try {
      V result = null;
      Pair<Integer, WeakReference<V>> pair = accessMap.get(key);
      if(pair != null){
        final int position = pair.getFirst();
        result = pair.getSecond().get();
        if(cache[position] != null && (cache[position].getFirst() == key || cache[position].getFirst().equals(key)))
          strategy.registerAccess(position);
      }

      if (result == null) {
        if (wayToGet != null)
          result = wayToGet.compute(key);
        if(result != null) {
          strategy.registerCacheMiss();
          put(key, result);
        }
      }
      return result;
    }
    finally {
      if(accessIndex++ % 10000 == 0)
        processReferenceQueue();
    }
  }

  public void flush() {
    for (int i = 0; i < cache.length; i++) {
      cache[i] = null;
    }
    strategy.clear();
  }

  private synchronized void processReferenceQueue() {
    Reference<? extends V> ref;
    while((ref = queue.poll()) != null){
      //noinspection SuspiciousMethodCalls
      final K key = invertedAccessMap.remove(ref);
      if (key != null)
        accessMap.remove(key);
    }
  }

  public boolean checkEqualSizes() {
    return accessMap.size() == cache.length && invertedAccessMap.size() == cache.length;
  }

  public CacheStrategy getStrategy() {
    return strategy;
  }

  public void clear() {
    accessMap.clear();
    invertedAccessMap.clear();
    strategy.clear();
    for (int i = 0; i < cache.length; i++) {
      if(cache[i] != null && cache[i].getSecond() instanceof CacheItem)
        ((CacheItem) cache[i].getSecond()).notifyRemove();
      cache[i] = null;
    }
  }
}
