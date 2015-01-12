package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;

/**
 * @author vp
 */
public class Double2BufferConverter implements Converter<Double, Buffer> {
  @Override
  public Double convertFrom(final Buffer source) {
    if(source.remaining() < 8) return null;
    return source.getDouble();
  }

  @Override
  public Buffer convertTo(final Double object) {
    final Buffer buffer = BufferFactory.wrap(new byte[8]);
    buffer.putDouble(object);
    buffer.position(0);
    return buffer;
  }
}