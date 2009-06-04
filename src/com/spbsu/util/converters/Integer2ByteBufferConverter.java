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
public class Integer2ByteBufferConverter implements Converter<Integer, ByteBuffer> {
  public Integer convertTo(ByteBuffer source) {
    if(source.remaining() < 4) return null;
    return source.getInt();
  }

  public ByteBuffer convertFrom(Integer object) {
    final ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.putInt(object);
    buffer.rewind();
    return buffer;
  }
}
