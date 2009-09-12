package com.spbsu.util.nio.impl;

import com.spbsu.util.nio.RWBuffer;
import com.spbsu.util.nio.WriteBuffer;

import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 15:50:48
 * To change this template use File | Settings | File Templates.
 */
public class CompositeBuffer implements RWBuffer {
  private static final ByteBuffer EMPTY = ByteBuffer.wrap(new byte[0]);
  private final ByteBuffer[] buffers;
  private final int capacity;

  private int position;
  private int limit;

  private ByteBuffer active;
  private int activeNo;
  private int localPos;

  public CompositeBuffer(ByteBuffer... buffers){
    this.buffers = buffers;
    int capacity = 0;
    active = EMPTY;
    for (ByteBuffer buffer : buffers) {
      if (active == EMPTY)
        active = buffer;
      capacity += buffer.remaining();
    }
    this.capacity = capacity;

    position = 0;
    limit = capacity;
    activeNo = 0;
    localPos = 0;
  }


  public int capacity() {
    return capacity;
  }

  public int remaining() {
    return limit - position;
  }

  public int limit() {
    return limit;
  }

  public void limit(int pos) {
    limit = pos;
  }

  public void position(int pos) {
    final int newLocalPosition = localPos - position - pos;
    position = pos;

    if (newLocalPosition >= 0 && newLocalPosition < active.remaining()) {
      localPos = newLocalPosition;
      return;
    }
    int index = 0;
    active = null;
    for (ByteBuffer buffer : buffers) {
      if (pos <= buffer.remaining()) {
        active = buffer;
        break;
      }
      index++;
      pos -= buffer.remaining();
    }
    activeNo = index;
    if (active == null)
      throw new BufferUnderflowException();
    localPos = active.position() + pos;
  }

  public int position() {
    return position;
  }

  public CompositeBuffer duplicate() {
    return new CompositeBuffer(buffers);
  }

  public WriteBuffer putByte(byte b) {
    if (active.remaining() < localPos + 1) {
      if (activeNo >= buffers.length)
        throw new BufferOverflowException();
      active = buffers[++activeNo];
      localPos = 0;
      return putByte(b);
    }
    active.put(localPos++, b);
    position++;
    return this;
  }

  public WriteBuffer putByte(int pos, byte b) {
    final int oldPosition = position;
    try {
      position(pos);
      putByte(b);
      return this;
    }
    finally {
      position(oldPosition);
    }
  }

  public WriteBuffer putChar(char c) {
    if (active.remaining() < localPos + 2)
      return putByte((byte)((c >> 8) & 0xFF)).putByte((byte)(c & 0xFF));
    active.putChar(localPos += 2, c);
    position += 2;
    return this;
  }

  public WriteBuffer putChar(int pos, char c) {
    final int oldPosition = position;
    try {
      position(pos);
      putChar(c);
      return this;
    }
    finally {
      position(oldPosition);
    }
  }

  public WriteBuffer putInt(int i) {
    if (active.remaining() < localPos + 4)
      return putByte((byte)((i >> 24) & 0xFF))
              .putByte((byte)((i >> 16) & 0xFF))
              .putByte((byte)((i >> 8) & 0xFF))
              .putByte((byte)(i & 0xFF));
    active.putInt(localPos += 4, i);
    position += 4;
    return this;
  }

  public WriteBuffer putInt(int pos, int i) {
    final int oldPosition = position;
    try {
      position(pos);
      putInt(i);
      return this;
    }
    finally {
      position(oldPosition);
    }
  }

  public WriteBuffer putLong(long l) {
    if (active.remaining() < localPos + 8)
      return putByte((byte)((l >> 56) & 0xFF))
              .putByte((byte)((l >> 48) & 0xFF))
              .putByte((byte)((l >> 40) & 0xFF))
              .putByte((byte)((l >> 32) & 0xFF))
              .putByte((byte)((l >> 24) & 0xFF))
              .putByte((byte)((l >> 16) & 0xFF))
              .putByte((byte)((l >> 8) & 0xFF))
              .putByte((byte)(l & 0xFF));
    active.putLong(localPos += 8, l);
    position += 8;
    return this;
  }

  public WriteBuffer putLong(int pos, long l) {
    final int oldPosition = position;
    try {
      position(pos);
      putLong(l);
      return this;
    }
    finally {
      position(oldPosition);
    }
  }

  public WriteBuffer putFloat(float f) {
    return putInt(Float.floatToIntBits(f));
  }

  public WriteBuffer putFloat(int pos, float f) {
    return putInt(pos, Float.floatToIntBits(f));
  }

  public WriteBuffer putDouble(double d) {
    return putLong(Double.doubleToLongBits(d));
  }

  public WriteBuffer putDouble(int pos, double d) {
    return putLong(pos, Double.doubleToLongBits(d));
  }

  public byte getByte() {
    if (active.remaining() < localPos + 1) {
      if (activeNo >= buffers.length)
        throw new BufferUnderflowException();
      active = buffers[++activeNo];
      localPos = 0;
      return getByte();
    }
    position++;
    return active.get(localPos++);
  }

  public byte getByte(int pos) {
    final int oldPosition = position;
    try {
      position(pos);
      return getByte();
    }
    finally {
      position(oldPosition);
    }
  }

  public char getChar() {
    if (active.remaining() < localPos + 2)
      return (char)((((char)getByte() & 0xFF) << 8) | ((char)getByte()) & 0xFF);
    position += 2;
    return active.getChar(localPos += 2);
  }

  public char getChar(int pos) {
    final int oldPosition = position;
    try {
      position(pos);
      return getChar();
    }
    finally {
      position(oldPosition);
    }
  }

  public int getInt() {
    if (active.remaining() < localPos + 4)
      return (((int)getByte() & 0xFF) << 24) |
             (((int)getByte() & 0xFF) << 16) |
             (((int)getByte() & 0xFF) << 8)  |
             ((int)getByte() & 0xFF);
    position += 4;
    return active.getInt(localPos += 4);
  }

  public int getInt(int pos) {
    final int oldPosition = position;
    try {
      position(pos);
      return getInt();
    }
    finally {
      position(oldPosition);
    }
  }

  public long getLong() {
    if (active.remaining() < localPos + 8)
      return ((long)getByte() << 56) +
             ((long)getByte() << 48) +
             ((long)getByte() << 40) +
             ((long)getByte() << 32) +
             ((long)getByte() << 24) +
             ((long)getByte() << 16) +
             ((long)getByte() << 8) +
             (long)getByte();
    position += 8;
    return active.getLong(localPos += 8);
  }

  public long getLong(int pos) {
    final int oldPosition = position;
    try {
      position(pos);
      return getLong();
    }
    finally {
      position(oldPosition);
    }
  }

  public float getFloat() {
    return Float.intBitsToFloat(getInt());
  }

  public float getFloat(int pos) {
    return Float.intBitsToFloat(getInt(pos));
  }

  public double getDouble() {
    return Double.longBitsToDouble(getLong());
  }

  public double getDouble(int pos) {
    return Double.longBitsToDouble(getLong(pos));
  }
}
