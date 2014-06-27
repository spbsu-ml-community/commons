package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;


import java.nio.BufferUnderflowException;

/**
 * User: Igor Kuralenok
 * Date: 02.09.2006
 */
public abstract class CharSequence2BufferConverterOld implements Converter<CharSequence, Buffer> {

  protected abstract CharSequence createCharsequence(char[] chars);

  public CharSequence convertFrom(final Buffer source) {
    if (source.remaining() < 1)
      throw new BufferUnderflowException();
    final int length = NioConverterTools.restoreSize(source);
    if (length < 0 || source.remaining() < length)
      throw new BufferUnderflowException();
    final char[] chars = new char[length];
    for (int i = 0; i < chars.length; i++) {
      chars[i] = source.getChar();
    }
    return createCharsequence(chars);
  }

  public Buffer convertTo(final CharSequence cs) {
    final int length = cs.length();
    final Buffer buffer = NioConverterTools.storeSize(length);
    final Buffer content = BufferFactory.wrap(new byte[2 * length]);
    for (int i = 0; i < length; i++) {
      content.putChar(cs.charAt(i));
    }
    content.position(0);
    return BufferFactory.join(buffer, content);
  }
}