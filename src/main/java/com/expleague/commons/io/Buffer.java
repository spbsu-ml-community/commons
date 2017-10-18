package com.expleague.commons.io;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 15:10:42
 * To change this template use File | Settings | File Templates.
 */
public interface Buffer {
// Control
  int capacity();
  int remaining();

  Buffer limit(int pos);
  int limit();

  Buffer position(int pos);
  int position();

// Input
  byte getByte();
  byte getByte(int pos);

  char getChar();
  char getChar(int pos);

  short getShort();
  short getShort(int pos);

  int getInt();
  int getInt(int pos);

  long getLong();
  long getLong(int pos);

  float getFloat();
  float getFloat(int pos);

  double getDouble();
  double getDouble(int pos);

// Output
  Buffer putByte(byte b);
  Buffer putByte(int pos, byte b);

  Buffer putChar(char c);
  Buffer putChar(int pos, char c);

  Buffer putInt(int i);
  Buffer putInt(int pos, int i);

  Buffer putShort(short i);
  Buffer putShort(int pos, short i);

  Buffer putLong(long l);
  Buffer putLong(int pos, long l);

  Buffer putFloat(float f);
  Buffer putFloat(int pos, float f);

  Buffer putDouble(double d);
  Buffer putDouble(int pos, double d);

  public Buffer put(byte[] array);
  public Buffer put(byte[] array, int off, int len);

  public int get(byte[] array);
  public int get(byte[] array, int off, int len);
}
