package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;

/**
 * @author bgleb
 */
public class EnumConverter<T extends Enum<T>> implements Converter<T, String> {
  final Class<T> clazz;

  public EnumConverter(final Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public T convertFrom(final String source) {
    return Enum.valueOf(clazz, source);
  }

  @Override
  public String convertTo(final T object) {
    return object.name();
  }

}
