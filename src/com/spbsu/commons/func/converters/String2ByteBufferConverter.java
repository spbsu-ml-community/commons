package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;

import java.nio.ByteBuffer;

/**
 * User: Igor Kuralenok
 * Date: 02.09.2006
 * Time: 15:27:51
 */
public class String2ByteBufferConverter implements Converter<String, ByteBuffer> {
  @Override
  public String convertFrom(ByteBuffer source) {
    final int length = source.getShort();
    final char[] chars = new char[length];
    source.asCharBuffer().get(chars);
    return new String(chars);
  }

  @Override
  public ByteBuffer convertTo(String object) {
    final ByteBuffer buffer = ByteBuffer.allocate((object.length() + 2) * 2);
    buffer.putShort((short) object.length());
    buffer.asCharBuffer().put(object.toCharArray());
    buffer.rewind();
    return buffer;
  }
}
