package com.expleague.commons.func.converters.test.b;


import com.expleague.commons.func.types.TypeConverter;

/**
 * User: solar
 * Date: 24.06.13
 * Time: 17:17
 */

class A implements TypeConverter<Integer,String> {
  @Override
  public String convert(final Integer from) {
      return Integer.toString(from);
  }
}


public class SampleConverter extends A {
}
