package com.spbsu.commons.io.converters.util;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;
import com.spbsu.commons.io.converters.NioConverterTools;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vp
 */
public class Array2BufferConverter<T> implements Converter<T[], Buffer> {
  private final T[] emptyArray;
  private final Converter<T, Buffer> converter;

  public Array2BufferConverter(T[] emptyArray, final Converter<T, Buffer> converter) {
    this.emptyArray = emptyArray;
    this.converter = converter;
  }

  public Array2BufferConverter(final Converter<T, Buffer> converter) {
    //noinspection unchecked
    this.emptyArray = (T[]) new Object[0];
    this.converter = converter;
  }

  @Override
  public T[] convertFrom(final Buffer source) {
    if (source.remaining() < 1) return null;
    final int size = NioConverterTools.restoreSize(source);
    final List<T> result = new ArrayList<T>(size);
    for (int i = 0; i < size; i++) {
      final T t = converter.convertFrom(source);
      if (t == null) return null;
      result.add(t);
    }
    //noinspection unchecked
    return (T[]) result.toArray(emptyArray);
  }

  @Override
  public Buffer convertTo(final T[] array) {
    final Buffer[] buffersArray = new Buffer[1 + array.length];
    for (int i = 0; i < array.length; i++) {
      buffersArray[1 + i] = converter.convertTo(array[i]);
    }
    buffersArray[0] = NioConverterTools.storeSize(array.length);
    return BufferFactory.join(buffersArray);
  }
}
