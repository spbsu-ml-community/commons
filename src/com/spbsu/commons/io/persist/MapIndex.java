package com.spbsu.commons.io.persist;

import com.spbsu.commons.util.Flushable;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 14.10.2009
 * Time: 14:37:31
 * To change this template use File | Settings | File Templates.
 */
public interface MapIndex<K> extends Flushable {
  PageFileAddress get(K key);
  void set(K key, PageFileAddress address);

  Set<K> keySet();

  void flush();
  int size();
}
