package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;

import java.util.Arrays;
import java.util.List;

/**
 * User: terry
 * Date: 06.12.2009
 */
public abstract class List2BufferConverter<T> implements Converter<List<T>, Buffer> {
  final Array2BufferConverter<T> arrayConverter;

  public List2BufferConverter(Converter<T, Buffer> converter) {
    this.arrayConverter = new Array2BufferConverter<T>(converter);
  }

  protected abstract List<T> createList();

  public List<T> convertFrom(Buffer source) {
    final T[] array = arrayConverter.convertFrom(source);
    final List<T> result = createList();
    result.addAll(Arrays.asList(array));
    return result;
  }

  public Buffer convertTo(List<T> list) {
    //noinspection unchecked
    return arrayConverter.convertTo((T[]) list.toArray());
  }
}
