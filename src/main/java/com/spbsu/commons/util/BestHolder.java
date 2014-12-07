package com.spbsu.commons.util;

/**
 * User: Igor Kuralenok
 * Date: 03.09.2006
 * Time: 12:22:43
 */
public class BestHolder<T> {
  private T value;
  private volatile double score = Double.NEGATIVE_INFINITY;

  public BestHolder() {}

  public static <T> BestHolder<T> create() {
    return new BestHolder<T>();
  }

  public static <T> BestHolder<T> create(T initValue, double initScore) {
    return new BestHolder<T>(initValue, initScore);
  }

  public BestHolder(T value, double score) {
    this.value = value;
    this.score = score;
  }

  public double getScore() {
    return score;
  }
  @Override
  public String toString() {
    return filled() ? getValue().toString() + ":" + score : "(null)" + ":" + score;
  }

  public boolean filled() {
    return value != null;
  }

  public T getValue() {
    return value;
  }

  public synchronized boolean update(T update, double score) {
    if (score > this.score) {
      this.score = score;
      value = update;
      return true;
    }
    return false;
  }
}
