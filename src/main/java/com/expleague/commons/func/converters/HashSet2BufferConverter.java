package com.expleague.commons.func.converters;

import com.expleague.commons.io.Buffer;
import com.expleague.commons.func.Converter;

import java.util.HashSet;
import java.util.Set;

/**
 * User: terry
 */
public class HashSet2BufferConverter<T> extends Set2BufferConverter<T> {
  public HashSet2BufferConverter(final Converter<T, Buffer> tBufferConverter) {
    super(tBufferConverter);
  }

  @Override
  protected Set<T> createSet() {
    return new HashSet<>();
  }
}
