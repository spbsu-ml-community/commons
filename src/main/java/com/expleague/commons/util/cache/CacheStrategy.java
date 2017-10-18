package com.expleague.commons.util.cache;

import java.lang.reflect.InvocationTargetException;


import com.expleague.commons.util.cache.impl.LRUStrategy;
import com.expleague.commons.util.cache.impl.LFUStrategy;

/**
 * User: Igor Kuralenok
 * Date: 31.08.2006
 */
public interface CacheStrategy {
  int getStorePosition();

  void registerAccess(int position);

  void registerCacheMiss();

  int getAccessCount();
  int getCacheMisses();

  void clear();

  void removePosition(int position);

  public enum Type {
    LRU(LRUStrategy.class),
    LFU(LFUStrategy.class);
    private final Class<? extends CacheStrategy> clazz;
    private Type(final Class<? extends CacheStrategy> clazz) {
      this.clazz = clazz;
    }

    public CacheStrategy newInstance(final int size) {
      try {
        return clazz.getConstructor(int.class).newInstance(size);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException("Exception during CacheStrategy create", e);
      }
    }
  }
}
