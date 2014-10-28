package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;

/**
 * User: terry
 * Date: 23.06.2009
 */
public class Integer2CharSequenceConverter implements Converter<Integer, CharSequence> {

  @Override
  public Integer convertFrom(CharSequence source) {
    return Integer.parseInt(source.toString());
  }

  @Override
  public String convertTo(Integer value) {
    return value.toString();
  }
}
