package com.spbsu.util.nio;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 15:17:23
 * To change this template use File | Settings | File Templates.
 */
public interface WriteBuffer extends Buffer{
  WriteBuffer putByte(byte b);
  WriteBuffer putByte(int pos, byte b);

  WriteBuffer putChar(char c);
  WriteBuffer putChar(int pos, char c);

  WriteBuffer putInt(int i);
  WriteBuffer putInt(int pos, int i);

  WriteBuffer putLong(long l);
  WriteBuffer putLong(int pos, long l);

  WriteBuffer putFloat(float f);
  WriteBuffer putFloat(int pos, float f);

  WriteBuffer putDouble(double d);
  WriteBuffer putDouble(int pos, double d);
}
