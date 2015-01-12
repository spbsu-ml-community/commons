package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Computable;
import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;


import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * User: Igor Kuralenok
 * Date: 02.09.2006
 */
public class CharSequence2BufferConverter<T extends CharSequence> implements Converter<T, Buffer> {
  static CharsetDecoder DECODER;
  static CharsetEncoder ENCODER;
  private final Computable<char[], ? extends T> factory;

  public CharSequence2BufferConverter(final Computable<char[], ? extends T> factory) {
    this.factory = factory;
  }

  static {
    final Charset cs = Charset.forName("UTF-8");
    DECODER = cs.newDecoder();
    ENCODER = cs.newEncoder();
  }

  public static synchronized CharBuffer decode(final ByteBuffer byteBuffer) throws CharacterCodingException {
    return DECODER.decode(byteBuffer);
  }

  public static synchronized ByteBuffer encode(final CharBuffer charBuffer) throws CharacterCodingException {
    return ENCODER.encode(charBuffer);
  }

  public T convertFrom(final Buffer source) {
    if (source.remaining() < 1)
      throw new BufferUnderflowException();
    final int length = NioConverterTools.restoreSize(source);
    final byte[] bytes = new byte[length];
    if (source.get(bytes) != length)
      throw new RuntimeException("Corrupted char sequence");

    try {
      final CharBuffer buffer = decode(ByteBuffer.wrap(bytes));
      final char[] chars = new char[buffer.length()];
      buffer.get(chars);
      return factory.compute(chars);
    }
    catch (CharacterCodingException e) {
      throw new RuntimeException(e);
    }
  }

  public Buffer convertTo(final T cs) {
    try {
      final ByteBuffer contents = encode(CharBuffer.wrap(cs));
      return BufferFactory.join(NioConverterTools.storeSize(contents.remaining()), BufferFactory.wrap(contents));
    }
    catch (CharacterCodingException e) {
      throw new RuntimeException(e);
    }
  }
}