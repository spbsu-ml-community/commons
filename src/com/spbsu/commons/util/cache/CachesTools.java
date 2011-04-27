package com.spbsu.commons.util.cache;

import com.spbsu.commons.func.Computable;

/**
 * User: terry
 * Date: 28.11.2009
 */
public class CachesTools {

  public static <K, V> Cache<K, V> synchronizedCache(final Cache<K, V> source) {
    return new Cache<K, V>() {
      @Override
      public synchronized V put(K key, V value) {
        return source.put(key, value);
      }

      @Override
      public synchronized V get(K key) {
        return source.get(key);
      }

      @Override
      public synchronized void flush() {
        source.flush();
      }

      @Override
      public synchronized void clear() {
        source.clear();
      }

      @Override
      public synchronized V get(K key, Computable<K, V> wayToGet) {
        return source.get(key, wayToGet);
      }

      @Override
      public synchronized void clear(K key) {
        source.clear(key);
      }
    };
  }
}
