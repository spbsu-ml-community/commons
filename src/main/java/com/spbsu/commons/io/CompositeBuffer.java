package com.spbsu.commons.io;

import com.spbsu.commons.func.Processor;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 15:50:48
 */
public class CompositeBuffer implements Buffer {
  private static final Buffer EMPTY = BufferFactory.wrap(new byte[0]);
  private final Buffer[] buffers;
  private final int capacity;

  private int position;
  private int limit;

  private Buffer active;
  private int activeNo;
  private int localPos;

  private static Buffer[] copyTo(final ByteBuffer[] buffers) {
    final Buffer[] rw = new Buffer[buffers.length];
    for (int i = 0; i < buffers.length; i++)
      rw[i] = BufferFactory.wrap(buffers[i]);
    return rw;
  }

  public CompositeBuffer(final ByteBuffer... buffers) {
    this(copyTo(buffers));
  }

  public CompositeBuffer(final Buffer... buffers){
    this.buffers = buffers;
    int capacity = 0;
    active = EMPTY;
    for (final Buffer buffer : buffers) {
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

  public Buffer limit(final int pos) {
    limit = pos;
    return this;
  }

  public Buffer position(int pos) {
    final int newLocalPosition = localPos - position - pos;
    position = pos;

    if (newLocalPosition >= 0 && newLocalPosition < active.remaining()) {
      localPos = newLocalPosition;
      return this;
    }
    int index = 0;
    active = null;
    for (final Buffer buffer : buffers) {
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
    localPos = pos;
    return this;
  }

  public int position() {
    return position;
  }

  public CompositeBuffer duplicate() {
    return new CompositeBuffer(buffers);
  }

  public Buffer putByte(final byte b) {
    if (active.remaining() < localPos + 1) {
      if (activeNo >= buffers.length)
        throw new BufferOverflowException();
      active = buffers[++activeNo];
      localPos = 0;
      return putByte(b);
    }
    active.putByte(active.position() + localPos++, b);
    position++;
    return this;
  }

  public Buffer putByte(final int pos, final byte b) {
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

  public Buffer putChar(final char c) {
    if (active.remaining() < localPos + 2)
      return putByte((byte)((c >> 8) & 0xFF)).putByte((byte)(c & 0xFF));
    active.putChar(active.position() + localPos, c);
    localPos += 2;
    position += 2;
    return this;
  }

  public Buffer putChar(final int pos, final char c) {
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

  public Buffer putShort(final short i) {
    if (active.remaining() < localPos + 2)
      return putByte((byte)((i >> 8) & 0xFF)).putByte((byte)(i & 0xFF));
    active.putShort(active.position() + localPos, i);
    localPos += 2;
    position += 2;
    return this;
  }

  public Buffer putShort(final int pos, final short i) {
    final int oldPosition = position;
    try {
      position(pos);
      putShort(i);
      return this;
    }
    finally {
      position(oldPosition);
    }
  }

  public Buffer putInt(final int i) {
    if (active.remaining() < localPos + 4)
      return putByte((byte)((i >> 24) & 0xFF))
              .putByte((byte)((i >> 16) & 0xFF))
              .putByte((byte)((i >> 8) & 0xFF))
              .putByte((byte)(i & 0xFF));
    active.putInt(active.position() + localPos, i);
    localPos += 4;
    position += 4;
    return this;
  }

  public Buffer putInt(final int pos, final int i) {
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

  public Buffer putLong(final long l) {
    if (active.remaining() < localPos + 8)
      return putByte((byte)((l >> 56) & 0xFF))
              .putByte((byte)((l >> 48) & 0xFF))
              .putByte((byte)((l >> 40) & 0xFF))
              .putByte((byte)((l >> 32) & 0xFF))
              .putByte((byte)((l >> 24) & 0xFF))
              .putByte((byte)((l >> 16) & 0xFF))
              .putByte((byte)((l >> 8) & 0xFF))
              .putByte((byte)(l & 0xFF));
    active.putLong(active.position() + localPos, l);
    localPos += 8;
    position += 8;
    return this;
  }

  public Buffer putLong(final int pos, final long l) {
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

  public Buffer putFloat(final float f) {
    return putInt(Float.floatToIntBits(f));
  }

  public Buffer putFloat(final int pos, final float f) {
    return putInt(pos, Float.floatToIntBits(f));
  }

  public Buffer putDouble(final double d) {
    return putLong(Double.doubleToLongBits(d));
  }

  public Buffer putDouble(final int pos, final double d) {
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
    return active.getByte(active.position() + localPos++);
  }

  public byte getByte(final int pos) {
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
    return active.getChar(active.position() + (localPos += 2) - 2);
  }

  public char getChar(final int pos) {
    final int oldPosition = position;
    try {
      position(pos);
      return getChar();
    }
    finally {
      position(oldPosition);
    }
  }

  public short getShort() {
    if (active.remaining() < localPos + 2)
      return (short)((((short)getByte() & 0xFF) << 8) | ((short)getByte()) & 0xFF);
    position += 2;
    return active.getShort((localPos += 2) - 2 + active.position());
  }

  public short getShort(final int pos) {
    final int oldPosition = position;
    try {
      position(pos);
      return getShort();
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
    return active.getInt((localPos += 4) - 4 + active.position());
  }

  public int getInt(final int pos) {
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
             (((long)getByte() & 0xFF) << 48) +
             (((long)getByte() & 0xFF) << 40) +
             (((long)getByte() & 0xFF) << 32) +
             (((long)getByte() & 0xFF) << 24) +
             (((long)getByte() & 0xFF) << 16) +
             (((long)getByte() & 0xFF) << 8) +
             ((long)getByte() & 0xFF);
    position += 8;
    return active.getLong((localPos += 8) - 8 + active.position());
  }

  public long getLong(final int pos) {
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

  public float getFloat(final int pos) {
    return Float.intBitsToFloat(getInt(pos));
  }

  public double getDouble() {
    return Double.longBitsToDouble(getLong());
  }

  public double getDouble(final int pos) {
    return Double.longBitsToDouble(getLong(pos));
  }

  public Buffer put(final byte[] array) {
    return put(array, 0, array.length);
  }

  public Buffer put(final byte[] array, final int off, final int len) {
    if (len > remaining())
      throw new BufferUnderflowException();
    int index = 0;
    while (index < len) {
      final int old = active.position();
      final int copyFromActive = Math.min(active.remaining() - localPos, len - index);
      active.position(old + localPos);
      active.put(array, off + index, copyFromActive); // copy
      active.position(old); // restore old position
      index += copyFromActive;
      localPos += copyFromActive;
      position += copyFromActive;
      if (localPos >= active.remaining()) { // next part in composite
        if (activeNo == buffers.length - 1)
          break; // last in composite
        active = buffers[++activeNo];
        localPos = 0;
      }
    }
    return this;
  }

  public int get(final byte[] array) {
    return get(array, 0, array.length);
  }

  public int get(final byte[] array, final int off, final int len) {
    int index = 0;
    while (len > index) {
      final int old = active.position();
      final int copyFromActive = Math.min(active.remaining() - localPos, len - index);
      active.position(localPos + old);
      active.get(array, off + index, copyFromActive); // copy
      active.position(old); // restore old position
      index += copyFromActive;
      localPos += copyFromActive;
      position += copyFromActive;
      if (localPos >= active.remaining()) { // next part in composite
        if (activeNo == buffers.length - 1)
          break; // last in composite
        active = buffers[++activeNo];
        localPos = 0;
      }
    }
    return index;
  }

  public void visitParts(final Processor<Buffer> visitor) {
    int active = activeNo;
    int position = this.position;
    Buffer current = localPos > 0 ? BufferFactory.duplicate(this.active).position(localPos) : this.active;
    while (current != null) {
      if (current instanceof CompositeBuffer)
        ((CompositeBuffer) current).visitParts(visitor);
      else
        visitor.process(current);
      position += current.remaining();
      if (++active < buffers.length && position < limit) {
        if (limit - position < buffers[active].remaining())
          current = BufferFactory.duplicate(buffers[active]).limit(limit - position);
        else
          current = buffers[active];
      }
      else current = null;
    }
  }
}
