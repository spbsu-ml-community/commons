package com.expleague.commons.func.converters;

import com.expleague.commons.func.Converter;

/**
 * Created with IntelliJ IDEA.
 * User: shutovich
 * Date: 07.12.12
 * Time: 16:24
 * To change this template use File | Settings | File Templates.
 */
public class Double2StringConverter implements Converter<Double, String> {

  @Override
  public Double convertFrom(final String source) {
    return Double.parseDouble(source);
  }

  @Override
  public String convertTo(final Double value) {
    return value.toString();
  }
}
