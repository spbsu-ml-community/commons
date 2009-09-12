package com.spbsu.util.nio.impl;

import com.spbsu.util.nio.RWBuffer;
import com.spbsu.util.nio.WriteBuffer;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 15:32:51
 * To change this template use File | Settings | File Templates.
 */
public class ByteBufferWrapper implements RWBuffer {
  private final ByteBuffer buffer;
  public ByteBufferWrapper(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  public int capacity() {
    return buffer.capacity();
  }

  public int remaining() {
    return buffer.remaining();
  }

  public void limit(int pos) {
    buffer.limit(pos);
  }

  public int limit() {
    return buffer.limit();
  }

  public void position(int pos) {
    buffer.position(pos);
  }

  public int position() {
    return buffer.position();
  }

  public ByteBufferWrapper duplicate() {
    return new ByteBufferWrapper(buffer.duplicate());
  }

  public byte getByte() {
    return buffer.get();
  }

  public byte getByte(int pos) {
    return buffer.get(pos);
  }

  public char getChar() {
    return buffer.getChar();
  }

  public char getChar(int pos) {
    return buffer.getChar(pos);
  }

  public int getInt() {
    return buffer.getInt();
  }

  public int getInt(int pos) {
    return buffer.getInt(pos);
  }

  public long getLong() {
    return buffer.getLong();
  }

  public long getLong(int pos) {
    return buffer.getLong(pos);
  }

  public float getFloat() {
    return buffer.getFloat();
  }

  public float getFloat(int pos) {
    return buffer.getFloat(pos);
  }

  public double getDouble() {
    return buffer.getDouble();
  }

  public double getDouble(int pos) {
    return buffer.getDouble(pos);
  }

  public WriteBuffer putByte(byte b) {
    buffer.put(b);
    return this;
  }

  public WriteBuffer putByte(byte b, int pos) {
    buffer.put(pos, b);
    return this;
  }

  public WriteBuffer putChar(char c) {
    buffer.putChar(c);
    return this;
  }

  public WriteBuffer putChar(char c, int pos) {
    buffer.putChar(pos, c);
    return this;
  }

  public WriteBuffer putInt(int i) {
    buffer.putInt(i);
    return this;
  }

  public WriteBuffer putInt(int i, int pos) {
    buffer.putInt(pos, i);
    return this;
  }

  public WriteBuffer putLong(long l) {
    buffer.putLong(l);
    return this;
  }

  public WriteBuffer putLong(long l, int pos) {
    buffer.putLong(pos, l);
    return this;
  }

  public WriteBuffer putFloat(float f) {
    buffer.putFloat(f);
    return this;
  }

  public WriteBuffer putFloat(float f, int pos) {
    buffer.putFloat(pos, f);
    return this;
  }

  public WriteBuffer putDouble(double d) {
    buffer.putDouble(d);
    return this;
  }

  public WriteBuffer putDouble(double d, int pos) {
    buffer.putDouble(pos, d);
    return this;
  }
}
