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

  @Override
  public int capacity() {
    return buffer.capacity();
  }

  @Override
  public int remaining() {
    return buffer.remaining();
  }

  @Override
  public Buffer limit(final int pos) {
    buffer.limit(pos);
    return this;
  }

  @Override
  public int limit() {
    return buffer.limit();
  }

  @Override
  public Buffer position(final int pos) {
    buffer.position(pos);
    return this;
  }

  @Override
  public int position() {
    return buffer.position();
  }

  public ByteBufferWrapper duplicate() {
    return new ByteBufferWrapper(buffer);
  }

  @Override
  public byte getByte() {
    return buffer.get();
  }

  @Override
  public byte getByte(final int pos) {
    return buffer.get(pos);
  }

  @Override
  public char getChar() {
    return buffer.getChar();
  }

  @Override
  public char getChar(final int pos) {
    return buffer.getChar(pos);
  }

  @Override
  public short getShort() {
    return buffer.getShort();
  }

  @Override
  public short getShort(final int pos) {
    return buffer.getShort(pos);
  }

  @Override
  public int getInt() {
    return buffer.getInt();
  }

  @Override
  public int getInt(final int pos) {
    return buffer.getInt(pos);
  }

  @Override
  public long getLong() {
    return buffer.getLong();
  }

  @Override
  public long getLong(final int pos) {
    return buffer.getLong(pos);
  }

  @Override
  public float getFloat() {
    return buffer.getFloat();
  }

  @Override
  public float getFloat(final int pos) {
    return buffer.getFloat(pos);
  }

  @Override
  public double getDouble() {
    return buffer.getDouble();
  }

  @Override
  public double getDouble(final int pos) {
    return buffer.getDouble(pos);
  }

  @Override
  public Buffer putByte(final byte b) {
    buffer.put(b);
    return this;
  }

  @Override
  public Buffer putByte(final int pos, final byte b) {
    buffer.put(pos, b);
    return this;
  }

  @Override
  public Buffer putChar(final char c) {
    buffer.putChar(c);
    return this;
  }

  @Override
  public Buffer putChar(final int pos, final char c) {
    buffer.putChar(pos, c);
    return this;
  }

  @Override
  public Buffer putShort(final short i) {
    buffer.putShort(i);
    return this;
  }

  @Override
  public Buffer putShort(final int pos, final short i) {
    buffer.putShort(pos, i);
    return this;
  }

  @Override
  public Buffer putInt(final int i) {
    buffer.putInt(i);
    return this;
  }

  @Override
  public Buffer putInt(final int pos, final int i) {
    buffer.putInt(pos, i);
    return this;
  }

  @Override
  public Buffer putLong(final long l) {
    buffer.putLong(l);
    return this;
  }

  @Override
  public Buffer putLong(final int pos, final long l) {
    buffer.putLong(pos, l);
    return this;
  }

  @Override
  public Buffer putFloat(final float f) {
    buffer.putFloat(f);
    return this;
  }

  @Override
  public Buffer putFloat(final int pos, final float f) {
    buffer.putFloat(pos, f);
    return this;
  }

  @Override
  public Buffer putDouble(final double d) {
    buffer.putDouble(d);
    return this;
  }

  @Override
  public Buffer putDouble(final int pos, final double d) {
    buffer.putDouble(pos, d);
    return this;
  }

  @Override
  public Buffer put(final byte[] array) {
    buffer.put(array);
    return this;
  }

  @Override
  public Buffer put(final byte[] array, final int off, final int len) {
    buffer.put(array, off, len);
    return this;
  }

  @Override
  public int get(final byte[] array) {
    return get(array, 0, array.length);
  }

  @Override
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
