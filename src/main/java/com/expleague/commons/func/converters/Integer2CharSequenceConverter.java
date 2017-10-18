package com.expleague.commons.func.converters;

import com.expleague.commons.func.Converter;
import com.expleague.commons.seq.CharSeqTools;

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
