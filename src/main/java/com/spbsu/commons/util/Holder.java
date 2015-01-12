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

  public void setValue(final T value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return filled() ? value.toString() : "(null)"; 
  }
}
