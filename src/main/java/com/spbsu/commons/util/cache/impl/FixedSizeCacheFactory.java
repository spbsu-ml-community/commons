package com.spbsu.commons.util.cache.impl;

import com.spbsu.commons.util.cache.Cache;
import com.spbsu.commons.util.cache.CacheFactory;
import com.spbsu.commons.util.cache.CacheStrategy;
import com.spbsu.commons.util.cache.CachesTools;

/**
 * User: terry
 * Date: 07.01.2010
 */
public class FixedSizeCacheFactory implements CacheFactory {
  @Override
  public <K, V> Cache<K, V> createCache(int cacheSize, CacheStrategy.Type cacheStrategyType) {
    return new FixedSizeCache<K, V>(cacheSize, cacheStrategyType);
  }

  @Override
  public <K, V> Cache<K, V> createThreadSafeCache(int cacheSize, CacheStrategy.Type cacheStrategyType) {
    return CachesTools.synchronizedCache(new FixedSizeCache<K, V>(cacheSize, cacheStrategyType));
  }
}
