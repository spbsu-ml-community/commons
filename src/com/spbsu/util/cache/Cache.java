package com.spbsu.util.cache;

import org.jetbrains.annotations.Nullable;
import com.spbsu.util.Computable;

/**
 * User: alms
 * Date: 21.01.2009
 * Time: 14:31:41
 */
public interface Cache<K, V> {

  V put(K key, V value);

  @Nullable
  V get(K key);
  V get(K key, Computable<K, V> computor);

  void flush();
}
