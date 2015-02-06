package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.func.types.ConversionDependant;
import com.spbsu.commons.func.types.ConversionRepository;
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
@SuppressWarnings("UnusedDeclaration")
public class List2CharSequenceConverter implements Converter<List, CharSequence>, ConversionDependant {
  private ConversionRepository repository;

  @Override
  public List convertFrom(final CharSequence source) {
    final List result = new ArrayList<>();
    final CharSequence[] parts = CharSeqTools.split(source, ", ");
    for (CharSequence part : parts) {
      final CharSequence[] class2value = new CharSequence[2];
      CharSeqTools.split(part, ' ', class2value);
      try {
        class2value[1] = CharSeqTools.replace(class2value[1], "\\ ", " ");
        class2value[1] = CharSeqTools.replace(class2value[1], "\\\\", "\\");

        //noinspection unchecked
        result.add(repository.convert(class2value[1], Class.forName(class2value[0].toString())));
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  @Override
  public CharSequence convertTo(final List data) {
    final StringBuilder stringBuilder = new StringBuilder();
    for (final Object element : data) {
      if (stringBuilder.length() > 0)
        stringBuilder.append(", ");
      CharSequence convert = repository.convert(element, CharSequence.class);
      convert = CharSeqTools.replace(convert, "\\", "\\\\");
      convert = CharSeqTools.replace(convert, " ", "\\ ");
      stringBuilder.append(element.getClass().getName()).append(" ").append(convert);
    }
    return stringBuilder;
  }

  @Override
  public void setConversionRepository(ConversionRepository repository) {
    this.repository = repository;
  }
}
