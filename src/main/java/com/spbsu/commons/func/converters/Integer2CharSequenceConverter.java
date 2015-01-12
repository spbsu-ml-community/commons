package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.seq.CharSeqTools;

/**
 * User: terry
 * Date: 23.06.2009
 */
public class Integer2CharSequenceConverter implements Converter<Integer, CharSequence> {

  @Override
  public Integer convertFrom(final CharSequence source) {
    return CharSeqTools.parseInt(source.toString());
  }

  @Override
  public String convertTo(final Integer value) {
    return value.toString();
  }
}
