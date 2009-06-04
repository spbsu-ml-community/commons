package com.spbsu.util.converters;

import com.spbsu.util.Converter;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 02.09.2006
 * Time: 15:27:51
 * To change this template use File | Settings | File Templates.
 */
public class Long2ByteBufferConverter implements Converter<Long, ByteBuffer> {
  public Long convertTo(ByteBuffer source) {
    if(source.remaining() < 8) return null;
    return source.getLong();
  }

  public ByteBuffer convertFrom(Long object) {
    final ByteBuffer buffer = ByteBuffer.allocate(8);
    buffer.putLong(object);
    buffer.rewind();
    return buffer;
  }
}
