package com.spbsu.commons.io.converters.util;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;
import com.spbsu.commons.io.converters.NioConverterTools;

/**
 * User: terry
 * Date: 13.12.2009
 */
public class LongArray2BufferConverter implements Converter<long[], Buffer> {
  @Override
  public long[] convertFrom(final Buffer source) {
    if (source.remaining() < 1) return null;
    final int size = NioConverterTools.restoreSize(source);
    final long[] array = new long[size];
    for (int i = 0; i < size; i++) {
      array[i] = source.getLong();
    }
    return array;
  }

  @Override
  public Buffer convertTo(final long[] array) {
    final Buffer bufferArray = BufferFactory.wrap(new byte[array.length * 8]);
    for (long anArray : array) {
      bufferArray.putLong(anArray);
    }
    bufferArray.position(0);
    return BufferFactory.join(NioConverterTools.storeSize(array.length), bufferArray);
  }
}
