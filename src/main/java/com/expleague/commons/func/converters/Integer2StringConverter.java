package com.expleague.commons.func.converters;

import com.expleague.commons.func.Converter;

/**
 * User: terry
 * Date: 23.06.2009
 */
public class Integer2StringConverter implements Converter<Integer, String> {

  @Override
  public Integer convertFrom(final String source) {
    return Integer.parseInt(source);
  }

  @Override
  public String convertTo(final Integer value) {
    return value.toString();
  }
}
