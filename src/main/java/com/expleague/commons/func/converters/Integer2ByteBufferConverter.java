package com.expleague.commons.func.converters;

import com.expleague.commons.func.Converter;

import java.nio.ByteBuffer;

/**
 * User: Igor Kuralenok
 * Date: 02.09.2006
 * Time: 15:27:51
 */
public class Integer2ByteBufferConverter implements Converter<Integer, ByteBuffer> {
  @Override
  public Integer convertFrom(final ByteBuffer source) {
    return source.getInt();
  }

  @Override
  public ByteBuffer convertTo(final Integer object) {
    final ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.putInt(object);
    buffer.rewind();
    return buffer;
  }
}
