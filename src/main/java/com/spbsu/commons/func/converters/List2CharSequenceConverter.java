package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.seq.CharSeqTools;


import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: shutovich
 * Date: 07.12.12
 * Time: 16:25
 * To change this template use File | Settings | File Templates.
 */
public class List2CharSequenceConverter<T> implements Converter<List<T>, CharSequence> {
  private final Converter<T, CharSequence> converter;
  private final char separator;

  public List2CharSequenceConverter(final Converter<T, CharSequence> converter, final char separator) {
    this.converter = converter;
    this.separator = separator;
  }

  @Override
  public List<T> convertFrom(final CharSequence source) {
    final List<T> result = new ArrayList<T>();
    final CharSequence[] parts = CharSeqTools.split(source, separator);
    for (final CharSequence part : parts) {
      result.add(converter.convertFrom(part));
    }
    return result;
  }

  @Override
  public CharSequence convertTo(final List<T> data) {
    final StringBuilder stringBuilder = new StringBuilder();
    for (final T element : data) {
      if (stringBuilder.length() > 0)
        stringBuilder.append(separator);
      stringBuilder.append(converter.convertTo(element));
    }
    return stringBuilder;
  }
}
