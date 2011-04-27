package com.spbsu.commons.net;

import com.spbsu.commons.util.Pair;

import java.util.Iterator;


/**
 * User: solar
 * Date: 10.06.2007
 * Time: 16:59:08
 */
public interface Task<T> {
  void start(T param);

  void setCompleted();
  boolean isCompleted();

  void setRequestProperty(String key, String value);
  public Iterator<Pair<String, String>> getPropertiesIterator();
}
