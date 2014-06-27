package com.spbsu.commons.func;

/**
 * User: terry
 * Date: 08.12.2009
 */
public class SelfConverter<T> implements Converter<T, T> {
  @Override
  public T convertFrom(T source) {
    return source;
  }

  @Override
  public T convertTo(T object) {
    return object;
  }

  public static <V> Converter<V, V> create() {
    return new SelfConverter<V>();
  }
}
