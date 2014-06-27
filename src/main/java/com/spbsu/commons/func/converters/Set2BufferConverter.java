package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;

import java.util.Arrays;
import java.util.Set;

/**
 * User: terry
 * Date: 12.12.2009
 */
public abstract class Set2BufferConverter<T> implements Converter<Set<T>, Buffer> {
  final Array2BufferConverter<T> arrayConverter;

  public Set2BufferConverter(Converter<T, Buffer> converter) {
    this.arrayConverter = new Array2BufferConverter<T>(converter);
  }

  protected abstract Set<T> createSet();

  public Set<T> convertFrom(Buffer source) {
    T[] array = arrayConverter.convertFrom(source);
    Set<T> set = createSet();
    set.addAll(Arrays.asList(array));
    return set;
  }

  public Buffer convertTo(Set<T> set) {
    //noinspection unchecked
    return arrayConverter.convertTo((T[]) set.toArray());
  }
}
