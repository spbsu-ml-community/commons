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
    if(source.remaining() < 4) return null;
    byte currentByte = source.get();
    int size = 0;
    while((currentByte & 0x80) != 0){
      size += currentByte & 0x7F;
      size <<= 7;
      currentByte = source.get();
    }
    size += currentByte;
//    int size = source.getInt();

    ArrayList<T> result = new ArrayList<T>(size);
    for (int i = 0; i < size; i++) {
      final T t = converter.convertTo(source);
      if(t == null) return null;
      result.add(t);
    }
    return result;
  }

  public ByteBuffer convertFrom(List<T> object) {
    List<ByteBuffer> buffersArray = new LinkedList<ByteBuffer>();
    int totalSize = 0;
    for (T t : object) {
      final ByteBuffer current = converter.convertFrom(t);
      buffersArray.add(current);
      totalSize += current.remaining();
    }
    int toEncode = object.size();
    final ByteBuffer buffer = ByteBuffer.allocate(totalSize + 4);

    if(toEncode >= (1 << (7 * 4))) throw new RuntimeException("Such long arrays are not supported by this converter");
    boolean started = false;
    for(int i = 0; i < 4; i++){
      byte current = (byte)(toEncode >> ((3 - i) * 7) & 0x7F);
      if(current != 0 || i == 3 || started){
        if(i != 3) current |= 0x80;
        buffer.put(current);
        started = true;
      }
      else
        buffer.limit(buffer.limit() - 1);
    }
//    buffer.putInt(object.length);
    for (ByteBuffer next : buffersArray) {
      buffer.put(next);
    }
    buffer.rewind();
    return buffer;
  }
}