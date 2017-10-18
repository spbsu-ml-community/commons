package com.expleague.commons.func.converters;

import com.expleague.commons.func.Converter;
import com.expleague.commons.io.Buffer;

import java.util.Arrays;
import java.util.Set;

/**
 * User: terry
 * Date: 12.12.2009
 */
public abstract class Set2BufferConverter<T> implements Converter<Set<T>, Buffer> {
  final Array2BufferConverter<T> arrayConverter;

  public Set2BufferConverter(final Converter<T, Buffer> converter) {
    this.arrayConverter = new Array2BufferConverter<T>(converter);
  }

  protected abstract Set<T> createSet();

  @Override
  public Set<T> convertFrom(final Buffer source) {
    final T[] array = arrayConverter.convertFrom(source);
    final Set<T> set = createSet();
    set.addAll(Arrays.asList(array));
    return set;
  }

  @Override
  public Buffer convertTo(final Set<T> set) {
    //noinspection unchecked
    return arrayConverter.convertTo((T[]) set.toArray());
  }
}
