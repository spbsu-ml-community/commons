package com.expleague.commons.func.converters;

import com.expleague.commons.func.Converter;
import com.expleague.commons.util.Factories;
import com.expleague.commons.io.Buffer;

import java.util.List;

/**
 * User: terry
 * Date: 13.12.2009
 */
public class ArrayList2BufferConverter<T> extends List2BufferConverter<T> {
  public ArrayList2BufferConverter(final Converter<T, Buffer> dataConverter) {
    super(dataConverter);
  }

  @Override
  protected List<T> createList() {
    return Factories.arrayList();
  }
}
