package com.spbsu.util.nio;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 15:06:46
 * To change this template use File | Settings | File Templates.
 */
public interface ReadBuffer extends Buffer {
  byte getByte();
  byte getByte(int pos);

  char getChar();
  char getChar(int pos);

  int getInt();
  int getInt(int pos);

  long getLong();
  long getLong(int pos);

  float getFloat();
  float getFloat(int pos);

  double getDouble();
  double getDouble(int pos);
}
