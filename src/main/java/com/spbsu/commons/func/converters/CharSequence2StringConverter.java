package com.spbsu.commons.func.converters;


import com.spbsu.commons.func.Converter;

/**
 * @author vp
 */
public class CharSequence2StringConverter implements Converter<CharSequence, String> {
  @Override
  public String convertTo(final CharSequence object) {
    return object.toString();
  }

  @Override
  public CharSequence convertFrom(final String source) {
    return source;
  }
}
