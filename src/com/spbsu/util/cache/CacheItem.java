package com.spbsu.util.cache;

/** never ever have this type of objects at strong reference!!!! */
public interface CacheItem {
  void notifyRemove();
}
