package com.spbsu.util.converters;

import com.spbsu.util.Converter;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 09.04.2008
 * Time: 14:30:18
 * To change this template use File | Settings | File Templates.
 */
public class IntegerArray2ByteBufferConverter  implements Converter<int[], ByteBuffer> {
  public int[] convertTo(ByteBuffer source) {
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

    int[] result = new int[size];
    if(source.remaining() < size * 4) return null;
    for (int i = 0; i < result.length; i++) {
      result[i] = source.getInt();
    }
    return result;
  }

  public ByteBuffer convertFrom(int[] object) {
    int toEncode = object.length;
    final ByteBuffer buffer = ByteBuffer.allocate(object.length * 4 + 4);

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
    for (int next : object) {
      buffer.putInt(next);
    }
    buffer.rewind();
    return buffer;
  }
}
