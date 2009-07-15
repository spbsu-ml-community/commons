package com.spbsu.util;

/**
 * User: solar
 * Date: 16.06.2007
 */
public abstract class TaskBase<T> implements Task<T>{
  boolean completedFlag = false;
  public synchronized void setCompleted() {
    completedFlag = true;
    notifyAll();
  }

  public synchronized boolean isCompleted() {
    return completedFlag;
  }
}
