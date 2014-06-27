package com.spbsu.commons.util.cache;

import com.spbsu.commons.func.Computable;
import com.spbsu.commons.util.Flushable;
import org.jetbrains.annotations.Nullable;

/**
 * User: alms
 * Date: 21.01.2009
 * Time: 14:31:41
 */
public interface Cache<K, V> extends Flushable {

  V put(K key, V value);

  @Nullable
  V get(K key);  

  void flush();

  void clear();

  V get(K key, Computable<K, V> wayToGet);

  void clear(K key);
}
