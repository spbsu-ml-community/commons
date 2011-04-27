package com.spbsu.commons.io.converters;

import com.spbsu.commons.func.Converter;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * User: Igor Kuralenok
 * Date: 02.09.2006
 */
@Deprecated
public class String2ByteBufferConverter implements Converter<String, ByteBuffer> {
  private static final Charset UTF = Charset.forName("UTF-8");

  public String convertFrom(ByteBuffer source) {
    if(source.remaining() < 1)
      return null;
    final int length = NioConverterTools.restoreSize(source);
    if(length < 0 || source.remaining() < length)
      return null;
    final byte[] chars = new byte[length];
    source.get(chars);
    return new String(chars, UTF);
  }

  public ByteBuffer convertTo(String object) {
    final byte[] bytes = object.getBytes(UTF);
    final ByteBuffer buffer = ByteBuffer.allocate(4 + bytes.length);
    NioConverterTools.storeSize(bytes.length, buffer);
    buffer.put(bytes);
    final int available = buffer.position();
    buffer.rewind();
    buffer.limit(available);
    return buffer;
  }
}
