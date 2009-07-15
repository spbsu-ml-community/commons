package com.spbsu.util.cache;

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

  public enum Type {
    LRU
  }
}
