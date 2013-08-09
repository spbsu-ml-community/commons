package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.text.CharSequenceTools;

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
  private static final char SEPARATOR = ',';

  public List2CharSequenceConverter(final Converter<T, CharSequence> converter) {
    this.converter = converter;
  }

  @Override
  public List<T> convertFrom(CharSequence source) {
    final List<T> result = new ArrayList<T>();
    CharSequence[] parts = CharSequenceTools.split(source, SEPARATOR);
    for (CharSequence part : parts) {
      result.add(converter.convertFrom(part));
    }
    return result;
  }

  @Override
  public CharSequence convertTo(List<T> data) {
    StringBuilder stringBuilder = new StringBuilder();
    for (T element : data) {
      if (stringBuilder.length() > 0)
        stringBuilder.append(SEPARATOR);
      stringBuilder.append(converter.convertTo(element));
    }
    return stringBuilder;
  }
}
