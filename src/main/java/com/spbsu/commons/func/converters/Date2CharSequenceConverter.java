package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.seq.CharSeqTools;


import java.util.Date;

/**
 * User: terry
 * Date: 16.08.2009
 */
public class Date2CharSequenceConverter implements Converter<Date, CharSequence> {
  @Override
  public Date convertFrom(final CharSequence source) {
    return new Date(CharSeqTools.parseLong(source));
  }

  @Override
  public String convertTo(final Date object) {
    return Long.toString(object.getTime());
  }
}
