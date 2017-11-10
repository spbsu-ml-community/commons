package com.expleague.commons.util.cache;

import java.util.function.Function;

/**
 * User: terry
 * Date: 28.11.2009
 */
public class CachesTools {

  public static <K, V> Cache<K, V> synchronizedCache(final Cache<K, V> source) {
    return new Cache<K, V>() {
      @Override
      public synchronized V put(final K key, final V value) {
        return source.put(key, value);
      }

      @Override
      public synchronized V get(final K key) {
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
      public synchronized V get(final K key, final Function<K, V> wayToGet) {
        return source.get(key, wayToGet);
      }

      @Override
      public synchronized void clear(final K key) {
        source.clear(key);
      }
    };
  }
}
