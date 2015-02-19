package com.spbsu.commons.func.converters;


import com.spbsu.commons.func.types.ConversionDependant;
import com.spbsu.commons.func.types.ConversionRepository;
import com.spbsu.commons.func.types.TypeConverter;
import com.spbsu.commons.seq.CharSeqTools;

import java.lang.reflect.Array;

/**
 * User: solar
 * Date: 21.10.14
 * Time: 17:10
 */
@SuppressWarnings("UnusedDeclaration")
public class ArrayConverters  {
  public static class To<T> implements TypeConverter<T[], CharSequence>, ConversionDependant {
    private ConversionRepository repository;

    @Override
    public CharSequence convert(final T[] from) {
      final StringBuilder builder = new StringBuilder();
      builder.append(from.getClass().getComponentType().getName()).append(", ").append(from.length).append(", ");
      builder.append("[");
      for(int i = 0; i < from.length; i++) {
        CharSequence convert = repository.convert(from[i], CharSequence.class);
        convert = CharSeqTools.replace(convert, "\\", "\\\\");
        convert = CharSeqTools.replace(convert, " ", "\\ ");
        builder.append(convert);
        if (i < from.length - 1)
          builder.append(", ");
      }
      builder.append("]");
      return builder.toString();
    }

    @Override
    public void setConversionRepository(ConversionRepository repository) {
      this.repository = repository;
    }
  }

  public static class From<T> implements TypeConverter<CharSequence, T[]>, ConversionDependant {
    private ConversionRepository repository;

    @Override
    public T[] convert(final CharSequence from) {
      final CharSequence[] split = CharSeqTools.split(from, ", ");
      try {
        final Class componentClass = Class.forName(split[0].toString());
        @SuppressWarnings("unchecked")
        final T[] result = (T[])Array.newInstance(componentClass, CharSeqTools.parseInt(split[1]));
        split[2] = split[2].subSequence(1, split[2].length()); // remove opening [
        split[split.length - 1] = split[split.length - 1].subSequence(0, split[split.length - 1].length() - 1); // remove closing ]
        for (int i = 0; i < result.length; i++) {
          CharSequence elementFrom = split[i + 2];
          elementFrom = CharSeqTools.replace(elementFrom, "\\ ", " ");
          elementFrom = CharSeqTools.replace(elementFrom, "\\\\", "\\");
          //noinspection unchecked
          result[i] = (T)repository.convert(elementFrom, componentClass);
        }
        return result;
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void setConversionRepository(ConversionRepository repository) {
      this.repository = repository;
    }
  }
}
