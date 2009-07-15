package com.spbsu.util;

/**
 * User: Igor Kuralenok
 * Date: 03.09.2006
 */
public class Holder<T> {
  T value;

  public Holder() {}

  public Holder(final T value) {
    this.value = value;
  }

  public boolean filled() {
    return value != null;
  }

  public T getValue() {
    return value;
  }

  public void setValue(final T value) {
    this.value = value;
  }

  public String toString() {
    return value != null ? value.toString() : null;
  }
}
