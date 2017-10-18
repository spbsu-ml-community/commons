package com.expleague.commons.func.converters;

import com.expleague.commons.func.Converter;

/**
 * User: lyadzhin
 * Date: 28.11.14 16:22
 */
public class IdentityConverter<T> implements Converter<T, T> {
  @Override
  public T convertTo(final T object) {
    return object;
  }

  @Override
  public T convertFrom(final T source) {
    return source;
  }
}
