package com.expleague.commons.func.converters;


import com.expleague.commons.func.Converter;

import java.nio.ByteBuffer;

/**
 * User: Igor Kuralenok
 * Date: 02.09.2006
 * Time: 15:27:51
 */
public class Long2ByteBufferConverter implements Converter<Long, ByteBuffer> {
  @Override
  public Long convertFrom(final ByteBuffer source) {
    return source.getLong();
  }

  @Override
  public ByteBuffer convertTo(final Long object) {
    final ByteBuffer buffer = ByteBuffer.allocate(8);
    buffer.putLong(object);
    buffer.rewind();
    return buffer;
  }
}
