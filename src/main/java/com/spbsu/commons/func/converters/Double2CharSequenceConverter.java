package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.seq.CharSeqTools;

/**
 * User: terry
 * Date: 23.06.2009
 */
public class Double2CharSequenceConverter implements Converter<Double, CharSequence> {

  @Override
  public Double convertFrom(CharSequence source) {
    return CharSeqTools.parseDouble(source);
  }

  @Override
  public String convertTo(Double value) {
    return value.toString();
  }
}
