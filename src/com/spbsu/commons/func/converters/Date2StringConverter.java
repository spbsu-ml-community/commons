package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;

import java.util.Date;

/**
 * User: terry
 * Date: 16.08.2009
 */
public class Date2StringConverter implements Converter<Date, String> {
  @Override
  public Date convertFrom(String source) {
    return new Date(Long.parseLong(source));
  }

  @Override
  public String convertTo(Date object) {
    return Long.toString(object.getTime());
  }
}
