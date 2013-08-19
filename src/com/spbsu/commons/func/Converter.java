package com.spbsu.commons.func;

/**
 * User: Igor Kuralenok
 * Date: 17.08.2006
 * Time: 15:09:41
 */
public interface Converter<F, T> {
  T convertTo(F object);
  F convertFrom(T source);
}
