package com.spbsu.commons.io.converters.util;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.util.Factories;

import java.util.Set;

/**
 * User: terry
 */
public class HashSet2BufferConverter<T> extends Set2BufferConverter<T> {
  public HashSet2BufferConverter(Converter<T, Buffer> tBufferConverter) {
    super(tBufferConverter);
  }

  @Override
  protected Set<T> createSet() {
    return Factories.hashSet();
  }
}
