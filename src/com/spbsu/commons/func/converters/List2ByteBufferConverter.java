package com.spbsu.commons.func.converters;


import com.spbsu.commons.func.Converter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * User: igorkuralenok
 * Date: 09.04.2008
 */
@Deprecated
public class List2ByteBufferConverter<T> implements Converter<List<T>, ByteBuffer> {
  final Converter<T, ByteBuffer> converter;

  public List2ByteBufferConverter(Converter<T, ByteBuffer> converter) {
    this.converter = converter;
  }

  public List<T> convertFrom(ByteBuffer source) {
    if(source.remaining() < 1) return null;
    final int size = NioConverterTools.restoreSize(source);
    final List<T> result = new ArrayList<T>(size);
    for (int i = 0; i < size; i++) {
      final T t = converter.convertFrom(source);
      if(t == null) return null;
      result.add(t);
    }
    return result;
  }

  public ByteBuffer convertTo(List<T> object) {
    final List<ByteBuffer> buffersArray = new LinkedList<ByteBuffer>();
    int totalSize = 0;
    for (T t : object) {
      final ByteBuffer current = converter.convertTo(t);
      buffersArray.add(current);
      totalSize += current.remaining();
    }
    final ByteBuffer buffer = ByteBuffer.allocate(totalSize + 4);

    NioConverterTools.storeSize(object.size(), buffer);
    for (ByteBuffer next : buffersArray) {
      buffer.put(next);
    }
    final int pos = buffer.position();
    buffer.rewind();
    buffer.limit(pos);
    return buffer;
  }

}