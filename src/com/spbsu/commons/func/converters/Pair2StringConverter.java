package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.util.Pair;

/**
 * @author vpdelta
 */
public class Pair2StringConverter<A, B> implements Converter<Pair<A, B>, String> {
  private final Converter<A, String> aConverter;
  private final Converter<B, String> bConverter;

  public Pair2StringConverter(
    final Converter<A, String> aConverter,
    final Converter<B, String> bConverter
  ) {
    this.aConverter = aConverter;
    this.bConverter = bConverter;
  }

  @Override
  public Pair<A, B> convertFrom(String source) {
    final String[] parts = source.split(",");
    return Pair.create(aConverter.convertFrom(parts[0]), bConverter.convertFrom(parts[1]));
  }

  @Override
  public String convertTo(Pair<A, B> pair) {
    return aConverter.convertTo(pair.getFirst()) + "," + bConverter.convertTo(pair.getSecond());
  }
}
