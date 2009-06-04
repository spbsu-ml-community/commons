package com.spbsu.util.cache;

import com.spbsu.util.Pair;
import com.spbsu.util.Computable;
import com.spbsu.util.cache.impl.LRUStrategy;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 31.08.2006
 * Time: 14:50:48
 * To change this template use File | Settings | File Templates.
 */
public class FixedSizeCache<K, V> {
  private final Map<K, Pair<Integer, WeakReference<V>>> accessMap = new HashMap<K, Pair<Integer, WeakReference<V>>>();
  private final Map<WeakReference<V>, K> invertedAccessMap = new HashMap<WeakReference<V>, K>();
  private final Pair<K, V>[] cache;
  private final ReferenceQueue<V> queue = new ReferenceQueue<V>();
  private final CacheStrategy strategy;

  public FixedSizeCache(int size, CacheStrategy.Type strategyType) {
    switch(strategyType){
      case LRU: strategy = new LRUStrategy(size); break;
      default: strategy = null;
    }
    //noinspection unchecked
    cache = (Pair<K, V>[])new Pair[size];
  }

  public void put(K key, V value) {
    if(value == null) return;
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
  }

  public V get(K key){
    final Pair<Integer, WeakReference<V>> pair = accessMap.get(key);
    if(pair == null){
      strategy.registerCacheMiss();
      return null;
    }
    final int position = pair.getFirst();
    final WeakReference<V> resultRef = pair.getSecond();
    final V result = resultRef.get();
    if(result == null || cache[position].getFirst() != key){
      strategy.registerCacheMiss();
      processReferenceQueue();
    }
    else{
      strategy.registerAccess(position);
    }
    return result;
  }

  public V get(K key, Computable<K, V> wayToGet){
    final Pair<Integer, WeakReference<V>> pair = accessMap.get(key);
    if(pair == null){
      strategy.registerCacheMiss();
      processReferenceQueue();
      final V result = wayToGet.compute(key);
      put(key, result);
      return result;
    }
    final int position = pair.getFirst();
    V result = pair.getSecond().get();
    if(result == null){
      result = wayToGet.compute(key);
      if(result != null){
        strategy.registerCacheMiss();
        processReferenceQueue();
        put(key, result);
      }
    }
    else if(cache[position].getFirst() == key || cache[position].getFirst().equals(key)){
      strategy.registerAccess(position);
    }
    else if(result instanceof CacheItem){ // the item was notified
      System.out.println("Strange");
    }
    return result;
  }

  private synchronized void processReferenceQueue() {
    Reference<? extends V> ref;
    while((ref = queue.poll()) != null){
      //noinspection SuspiciousMethodCalls
      final K key = invertedAccessMap.remove(ref);
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
      if(cache[i] != null && cache[i].getSecond() instanceof CacheItem) ((CacheItem) cache[i].getSecond()).notifyRemove();
      cache[i] = null;
    }
  }
}
