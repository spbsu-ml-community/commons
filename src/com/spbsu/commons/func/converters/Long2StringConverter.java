package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;

/**
 * User: alms
 * Date: 04.10.2009
 */
public class Long2StringConverter implements Converter<Long, String> {

  @Override
  public Long convertFrom(final String source) {
    return Long.parseLong(source);
  }

  @Override
  public String convertTo(final Long value) {
    return value.toString();
  }
}
