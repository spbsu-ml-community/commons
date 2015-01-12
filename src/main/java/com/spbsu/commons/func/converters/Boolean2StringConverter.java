package com.spbsu.commons.func.converters;


import com.spbsu.commons.func.Converter;

/**
 * User: terry
 * Date: 23.06.2009
 */
public class Boolean2StringConverter implements Converter<Boolean, String> {

  @Override
  public Boolean convertFrom(final String source) {
    return Boolean.parseBoolean(source);
  }

  @Override
  public String convertTo(final Boolean value) {
    return value.toString();
  }
}
