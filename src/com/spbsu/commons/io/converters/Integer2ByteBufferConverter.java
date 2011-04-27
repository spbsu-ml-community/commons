package com.spbsu.commons.io.converters;


import com.spbsu.commons.func.Converter;

import java.nio.ByteBuffer;

/**
 * User: Igor Kuralenok
 * Date: 02.09.2006
 */
@Deprecated
public class Integer2ByteBufferConverter implements Converter<Integer, ByteBuffer> {
  public Integer convertFrom(ByteBuffer source) {
    if(source.remaining() < 4) return null;
    return source.getInt();
  }

  public ByteBuffer convertTo(Integer object) {
    final ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.putInt(object);
    buffer.rewind();
    return buffer;
  }
}
