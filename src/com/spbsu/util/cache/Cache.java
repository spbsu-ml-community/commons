package com.spbsu.util.cache;

import com.spbsu.util.Computable;
import org.jetbrains.annotations.Nullable;

/**
 * User: alms
 * Date: 21.01.2009
 */
public interface Cache<K, V> {

  V put(K key, V value);

  @Nullable
  V get(K key);

  V get(K key, Computable<K, V> computor);

  void flush();

  void clear();
}
