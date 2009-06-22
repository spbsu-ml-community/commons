package com.spbsu.util.converters;

import com.spbsu.util.Converter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 09.04.2008
 * Time: 14:30:18
 * To change this template use File | Settings | File Templates.
 */
public class List2ByteBufferConverter<T> implements Converter<List<T>, ByteBuffer> {
  final Converter<T, ByteBuffer> converter;

  public List2ByteBufferConverter(Converter<T, ByteBuffer> converter) {
    this.converter = converter;
  }

  public List<T> convertTo(ByteBuffer source) {
    if(source.remaining() < 1) return null;
    final int size = ConverterUtil.restoreSize(source);
    if(size > 10000)
      System.out.println("" + size);
    final List<T> result = new ArrayList<T>(size);
    for (int i = 0; i < size; i++) {
      final T t = converter.convertTo(source);
      if(t == null) return null;
      result.add(t);
    }
    return result;
  }

  public ByteBuffer convertFrom(List<T> object) {
    final List<ByteBuffer> buffersArray = new LinkedList<ByteBuffer>();
    int totalSize = 0;
    for (T t : object) {
      final ByteBuffer current = converter.convertFrom(t);
      buffersArray.add(current);
      totalSize += current.remaining();
    }
    final ByteBuffer buffer = ByteBuffer.allocate(totalSize + 4);

    ConverterUtil.storeSize(object.size(), buffer);
//    buffer.putInt(object.length);
    for (ByteBuffer next : buffersArray) {
      buffer.put(next);
    }
    final int pos = buffer.position();
    buffer.rewind();
    buffer.limit(pos);
    return buffer;
  }

}