package com.expleague.commons.seq.regexp.converters;


import com.expleague.commons.func.Converter;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.seq.regexp.Alphabet;
import com.expleague.commons.seq.regexp.Pattern;

/**
 * User: Manokk
 * Date: 10.09.11
 * Time: 15:18
 */
public class PatternVecConverter<T> implements Converter<Pattern<T>, Vec> {
  private final Alphabet<T> alphabet;

  public PatternVecConverter(final Alphabet<T> a) {
    this.alphabet = a;
  }

  @Override
  public Pattern<T> convertFrom(final Vec vec) {
    final int size = vec.dim();
    final Pattern<T> p = new Pattern<T>(alphabet);
    for (int i = 0; i < size; i+=2) {
      final int code = (int) vec.get(i);
      final int mod = (int) vec.get(i + 1);
      p.add(alphabet.get(code), Pattern.Modifier.values()[mod]);
    }
    return p;
  }

  @Override
  public Vec convertTo(final Pattern<T> pattern) {
    final int size = pattern.size();
    final double[] patternVec = new double[2 * size];
    for (int i = 0, j = 0; i < size; i++, j+=2) {
      patternVec[j] = alphabet.getOrder(pattern.condition(i));
      patternVec[j + 1] = pattern.modifier(i).ordinal();
    }
    return new ArrayVec(patternVec);
  }

}