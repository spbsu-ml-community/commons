package com.expleague.commons.util.cache;

import com.expleague.commons.func.Computable;
import org.jetbrains.annotations.Nullable;

import java.io.Flushable;

/**
 * User: alms
 * Date: 21.01.2009
 * Time: 14:31:41
 */
public interface Cache<K, V> extends Flushable {

  V put(K key, V value);

  @Nullable
  V get(K key);  

  @Override
  void flush();

  void clear();

  V get(K key, Computable<K, V> wayToGet);

  void clear(K key);
}
