package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;

/**
 * User: terry
 * Date: 15.12.2009
 */
public class IntArray2BufferConverter implements Converter<int[], Buffer> {
  @Override
  public int[] convertFrom(final Buffer source) {
    if (source.remaining() < 1) return null;
    final int size = NioConverterTools.restoreSize(source);
    final int[] array = new int[size];
    for (int i = 0; i < size; i++) {
      array[i] = source.getInt();
    }
    return array;
  }

  @Override
  public Buffer convertTo(final int[] array) {
    final Buffer bufferArray = BufferFactory.wrap(new byte[array.length * 4]);
    for (int anArray : array) {
      bufferArray.putInt(anArray);
    }
    bufferArray.position(0);
    final Buffer buffer = NioConverterTools.storeSize(array.length);
    return BufferFactory.join(buffer, bufferArray);
  }
}
