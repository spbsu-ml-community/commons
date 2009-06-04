package com.spbsu.util;

/**
 * Created by IntelliJ IDEA.
 * User: solar
 * Date: 16.06.2007
 * Time: 13:58:33
 * To change this template use File | Settings | File Templates.
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
