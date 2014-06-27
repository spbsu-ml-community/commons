package com.spbsu.commons.util.cache;

/**
 * User: terry
 * Date: 07.01.2010
 */
public interface CacheFactory {
  <K, V> Cache<K, V> createCache(int cacheSize, CacheStrategy.Type cacheStrategyType);
  <K, V> Cache<K, V> createThreadSafeCache(int cacheSize, CacheStrategy.Type cacheStrategyType);
}
