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
  WriteBuffer putByte(byte b, int pos);

  WriteBuffer putChar(char c);
  WriteBuffer putChar(char c, int pos);

  WriteBuffer putInt(int i);
  WriteBuffer putInt(int i, int pos);

  WriteBuffer putLong(long l);
  WriteBuffer putLong(long l, int pos);

  WriteBuffer putFloat(float f);
  WriteBuffer putFloat(float f, int pos);

  WriteBuffer putDouble(double d);
  WriteBuffer putDouble(double d, int pos);
}
