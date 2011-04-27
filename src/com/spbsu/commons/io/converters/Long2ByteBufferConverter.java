package com.spbsu.commons.io.converters;

import com.spbsu.commons.func.Converter;

import java.nio.ByteBuffer;

/**
 * User: Igor Kuralenok
 * Date: 02.09.2006
 */
@Deprecated
public class Long2ByteBufferConverter implements Converter<Long, ByteBuffer> {
  public Long convertFrom(ByteBuffer source) {
    if(source.remaining() < 8) return null;
    return source.getLong();
  }

  public ByteBuffer convertTo(Long object) {
    final ByteBuffer buffer = ByteBuffer.allocate(8);
    buffer.putLong(object);
    buffer.rewind();
    return buffer;
  }
}
