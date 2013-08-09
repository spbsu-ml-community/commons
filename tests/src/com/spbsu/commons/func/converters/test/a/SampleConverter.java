package com.spbsu.commons.func.converters.test.a;


import com.spbsu.commons.func.types.TypeConverter;

/**
 * User: solar
 * Date: 24.06.13
 * Time: 15:49
 */
public class SampleConverter implements TypeConverter<Integer,String> {
  @Override
  public String convert(final Integer from) {
    return Integer.toString(from);
  }
}
