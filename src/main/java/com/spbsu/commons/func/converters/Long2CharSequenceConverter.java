package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.seq.CharSeqTools;

/**
 * User: terry
 * Date: 23.06.2009
 */
public class Long2CharSequenceConverter implements Converter<Long, CharSequence> {

  @Override
  public Long convertFrom(final CharSequence source) {
    return CharSeqTools.parseLong(source);
  }

  @Override
  public String convertTo(final Long value) {
    return value.toString();
  }
}
