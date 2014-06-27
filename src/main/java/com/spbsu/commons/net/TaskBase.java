package com.spbsu.commons.net;

import com.spbsu.commons.util.Pair;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * User: solar
 * Date: 16.06.2007
 * Time: 13:58:33
 */
public abstract class TaskBase<T> implements Task<T> {
  private boolean completedFlag = false;
  private final Set<Pair<String, String>> requestProps = new HashSet<Pair<String, String>>();

  public synchronized void setCompleted() {
    completedFlag = true;
    notifyAll();
  }

  public synchronized boolean isCompleted() {
    return completedFlag;
  }

  public void setRequestProperty(String key, String value) {
    requestProps.add(Pair.create(key, value));
  }

  public Iterator<Pair<String, String>> getPropertiesIterator() {
    return requestProps.iterator();
  }
}
