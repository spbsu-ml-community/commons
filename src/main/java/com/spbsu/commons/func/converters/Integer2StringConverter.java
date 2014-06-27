package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;

/**
 * User: terry
 * Date: 23.06.2009
 */
public class Integer2StringConverter implements Converter<Integer, String> {

  @Override
  public Integer convertFrom(String source) {
    return Integer.parseInt(source);
  }

  @Override
  public String convertTo(Integer value) {
    return value.toString();
  }
}
