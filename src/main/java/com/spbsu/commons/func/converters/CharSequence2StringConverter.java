package com.spbsu.commons.func.converters;

import java.util.ArrayList;
import java.util.List;


import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;

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
