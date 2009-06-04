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
public class String2ByteBufferConverter implements Converter<String, ByteBuffer> {
  public String convertTo(ByteBuffer source) {
    if(source.remaining() < 2) return null;
    final int length = source.getShort();
    if(length < 0 || source.remaining() < length * 2)
      return null;
    final char[] chars = new char[length];
    source.asCharBuffer().get(chars);
    source.position(source.position() + length * 2);
    return new String(chars);
  }

  public ByteBuffer convertFrom(String object) {
    final ByteBuffer buffer = ByteBuffer.allocate((object.length() + 1) * 2);
    buffer.putShort((short) object.length());
    buffer.asCharBuffer().put(object.toCharArray());
    buffer.rewind();
    return buffer;
  }
}
