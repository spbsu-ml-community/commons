package com.spbsu.commons.util;

/**
 * User: Igor Kuralenok
 * Date: 03.09.2006
 * Time: 12:22:43
 */
public class Holder<T> {
  private T value;

  public Holder() {}

  public static <T> Holder<T> create() {
    return new Holder<T>();
  }

  public static <T> Holder<T> create(final T initValue) {
    return new Holder<T>(initValue);
  }

  public Holder(final T value) {
    this.value = value;
  }

  public boolean filled() {
    return value != null;
  }

  public T getValue() {
    return value;
  }

  public synchronized void setValue(final T value) {
    if (value != null)
      notifyAll();
    this.value = value;
  }

  public synchronized void fill() {
    while (!filled()) {
      try {
        wait(0);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public String toString() {
    return filled() ? value.toString() : "(null)"; 
  }
}
