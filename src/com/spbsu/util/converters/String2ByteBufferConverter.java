package com.spbsu.util.converters;

import com.spbsu.util.Converter;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 02.09.2006
 * Time: 15:27:51
 * To change this template use File | Settings | File Templates.
 */
public class String2ByteBufferConverter implements Converter<String, ByteBuffer> {
  private static final Charset UTF = Charset.forName("UTF-8");

  public String convertTo(ByteBuffer source) {
    if(source.remaining() < 1)
      return null;
    final int length = ConverterUtil.restoreSize(source);
    if(length < 0 || source.remaining() < length)
      return null;
    final byte[] chars = new byte[length];
    source.get(chars);
    return new String(chars, UTF);
  }

  public ByteBuffer convertFrom(String object) {
    final byte[] bytes = object.getBytes(UTF);
    final ByteBuffer buffer = ByteBuffer.allocate(4 + bytes.length);
    ConverterUtil.storeSize(bytes.length, buffer);
    buffer.put(bytes);
    final int available = buffer.position();
    buffer.rewind();
    buffer.limit(available);
    return buffer;
  }
}
