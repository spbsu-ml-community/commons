package com.expleague.commons.func.converters;

import com.expleague.commons.func.Converter;
import com.expleague.commons.io.Buffer;
import com.expleague.commons.io.BufferFactory;

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