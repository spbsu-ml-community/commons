package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;

/**
 * User: Igor Kuralenok
 * Date: 02.09.2006
 */
public class Long2BufferConverter implements Converter<Long, Buffer> {
  public Long convertFrom(final Buffer source) {
    if(source.remaining() < 8) return null;
    return source.getLong();
  }

  public Buffer convertTo(final Long object) {
    final Buffer buffer = BufferFactory.wrap(new byte[8]);
    buffer.putLong(object);
    buffer.position(0);
    return buffer;
  }
}