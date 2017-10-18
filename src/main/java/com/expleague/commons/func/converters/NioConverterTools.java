package com.expleague.commons.func.converters;

import com.expleague.commons.io.Buffer;
import com.expleague.commons.io.BufferFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: igorkuralenok
 * Date: 21.06.2009
 */
public class NioConverterTools {
  public static void storeSize(final int toEncode, final ByteBuffer buffer) {
    if(toEncode >= (1 << (7 * 4)))
      throw new RuntimeException("Such long arrays are not supported by this converter");
    boolean started = false;
    for(int i = 0; i < 4; i++){
      byte current = (byte)(toEncode >> ((3 - i) * 7) & 0x7F);
      if(current != 0 || i == 3 || started){
        if(i != 3) current |= 0x80;
        buffer.put(current);
        started = true;
      }
    }
  }

  public static int restoreSize(final ByteBuffer source) {
    byte currentByte = source.get();
    int size = 0;
    while((currentByte & 0x80) != 0){
      size += currentByte & 0x7F;
      size <<= 7;
      currentByte = source.get();
    }
    size += currentByte;
    return size;
  }

  public static void storeSize(final int toEncode, final DataOutput output) throws IOException {
    if(toEncode >= (1 << (7 * 4)))
      throw new RuntimeException("Such long arrays are not supported by this converter");
    boolean started = false;
    for(int i = 0; i < 4; i++){
      byte current = (byte)(toEncode >> ((3 - i) * 7) & 0x7F);
      if(current != 0 || i == 3 || started){
        if(i != 3) current |= 0x80;
        output.write(current);
        started = true;
      }
    }
  }

  public static int restoreSize(final DataInput input) throws IOException {
    byte currentByte = (byte) input.readUnsignedByte();
    int size = 0;
    while((currentByte & 0x80) != 0){
      size += currentByte & 0x7F;
      size <<= 7;
      currentByte = (byte) input.readUnsignedByte();
    }
    size += currentByte;
    return size;
  }

  public static Buffer storeSize(final int toEncode) {
    if(toEncode >= (1 << (7 * 4)))
      throw new RuntimeException("Such long arrays are not supported by this converter");
    final Buffer buffer = BufferFactory.wrap(new byte[4]);
    boolean started = false;
    for(int i = 0; i < 4; i++){
      byte current = (byte)(toEncode >> ((3 - i) * 7) & 0x7F);
      if(current != 0 || i == 3 || started){
        if(i != 3) current |= 0x80;
        buffer.putByte(current);
        started = true;
      }
    }
    buffer.limit(buffer.position());
    buffer.position(0);
    return buffer;
  }

  public static int restoreSize(final Buffer source) {
    byte currentByte = source.getByte();
    int size = 0;
    while((currentByte & 0x80) != 0){
      size += currentByte & 0x7F;
      size <<= 7;
      currentByte = source.getByte();
    }
    size += currentByte;
    return size;
  }
}
