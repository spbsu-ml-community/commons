package com.expleague.commons.func.converters;

import com.expleague.commons.func.Converter;
import com.expleague.commons.seq.CharSeqTools;

/**
 * User: terry
 * Date: 23.06.2009
 */
public class Double2CharSequenceConverter implements Converter<Double, CharSequence> {

  @Override
  public Double convertFrom(final CharSequence source) {
    return CharSeqTools.parseDouble(source);
  }

  @Override
  public String convertTo(final Double value) {
    return value.toString();
  }
}
