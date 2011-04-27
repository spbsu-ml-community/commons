package com.spbsu.commons.io.converters.util;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;

/**
 * User: Igor Kuralenok
 * Date: 02.09.2006
 */
public class Long2BufferConverter implements Converter<Long, Buffer> {
  public Long convertFrom(Buffer source) {
    if(source.remaining() < 8) return null;
    return source.getLong();
  }

  public Buffer convertTo(Long object) {
    final Buffer buffer = BufferFactory.wrap(new byte[8]);
    buffer.putLong(object);
    buffer.position(0);
    return buffer;
  }
}