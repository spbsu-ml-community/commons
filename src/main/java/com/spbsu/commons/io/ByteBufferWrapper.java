package com.spbsu.commons.io;

import java.nio.ByteBuffer;

/**
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 15:32:51
 */
public class ByteBufferWrapper implements Buffer {
  private final ByteBuffer buffer;

  public ByteBufferWrapper(final ByteBuffer buffer) {
    this.buffer = buffer.duplicate();
  }

  public int capacity() {
    return buffer.capacity();
  }

  public int remaining() {
    return buffer.remaining();
  }

  public Buffer limit(final int pos) {
    buffer.limit(pos);
    return this;
  }

  public int limit() {
    return buffer.limit();
  }

  public Buffer position(final int pos) {
    buffer.position(pos);
    return this;
  }

  public int position() {
    return buffer.position();
  }

  public ByteBufferWrapper duplicate() {
    return new ByteBufferWrapper(buffer);
  }

  public byte getByte() {
    return buffer.get();
  }

  public byte getByte(final int pos) {
    return buffer.get(pos);
  }

  public char getChar() {
    return buffer.getChar();
  }

  public char getChar(final int pos) {
    return buffer.getChar(pos);
  }

  public short getShort() {
    return buffer.getShort();
  }

  public short getShort(final int pos) {
    return buffer.getShort(pos);
  }

  public int getInt() {
    return buffer.getInt();
  }

  public int getInt(final int pos) {
    return buffer.getInt(pos);
  }

  public long getLong() {
    return buffer.getLong();
  }

  public long getLong(final int pos) {
    return buffer.getLong(pos);
  }

  public float getFloat() {
    return buffer.getFloat();
  }

  public float getFloat(final int pos) {
    return buffer.getFloat(pos);
  }

  public double getDouble() {
    return buffer.getDouble();
  }

  public double getDouble(final int pos) {
    return buffer.getDouble(pos);
  }

  public Buffer putByte(final byte b) {
    buffer.put(b);
    return this;
  }

  public Buffer putByte(final int pos, final byte b) {
    buffer.put(pos, b);
    return this;
  }

  public Buffer putChar(final char c) {
    buffer.putChar(c);
    return this;
  }

  public Buffer putChar(final int pos, final char c) {
    buffer.putChar(pos, c);
    return this;
  }

  public Buffer putShort(final short i) {
    buffer.putShort(i);
    return this;
  }

  public Buffer putShort(final int pos, final short i) {
    buffer.putShort(pos, i);
    return this;
  }

  public Buffer putInt(final int i) {
    buffer.putInt(i);
    return this;
  }

  public Buffer putInt(final int pos, final int i) {
    buffer.putInt(pos, i);
    return this;
  }

  public Buffer putLong(final long l) {
    buffer.putLong(l);
    return this;
  }

  public Buffer putLong(final int pos, final long l) {
    buffer.putLong(pos, l);
    return this;
  }

  public Buffer putFloat(final float f) {
    buffer.putFloat(f);
    return this;
  }

  public Buffer putFloat(final int pos, final float f) {
    buffer.putFloat(pos, f);
    return this;
  }

  public Buffer putDouble(final double d) {
    buffer.putDouble(d);
    return this;
  }

  public Buffer putDouble(final int pos, final double d) {
    buffer.putDouble(pos, d);
    return this;
  }

  public Buffer put(final byte[] array) {
    buffer.put(array);
    return this;
  }

  public Buffer put(final byte[] array, final int off, final int len) {
    buffer.put(array, off, len);
    return this;
  }

  public int get(final byte[] array) {
    return get(array, 0, array.length);
  }

  public int get(final byte[] array, final int off, final int len) {
    if (len > buffer.remaining()) {
      final int remaining = buffer.remaining();
      buffer.get(array, off, remaining);
      return remaining;
    }
    else {
      buffer.get(array, off, len);
      return len;
    }
  }

  public boolean hasArray() {
    return buffer.hasArray();
  }

  public byte[] array() {
    return buffer.array();
  }

  public boolean isDirect() {
    return buffer.isDirect();
  }
}
