package com.spbsu.util.cache;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 31.08.2006
 * Time: 15:14:35
 * To change this template use File | Settings | File Templates.
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
