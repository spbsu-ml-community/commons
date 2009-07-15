package com.spbsu.util;

/**
 * User: Igor Kuralenok
 * Date: 17.08.2006
 */
public interface Converter<T, S> {
  T convertTo(S source);
  S convertFrom(T object);
}
