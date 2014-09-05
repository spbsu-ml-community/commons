package com.spbsu.commons.seq;

import java.util.Arrays;

/**
 * User: solar
 * Date: 08.07.14
 * Time: 11:07
 */
public class ByteSeq extends Seq.Stub<Byte> {
  public final byte[] arr;
  public final int start;
  public final int end;

  public ByteSeq(final byte[] arr) {
    this(arr, 0, arr.length);
  }

  public ByteSeq(final byte[] arr, int start, int end) {
    if (start < 0 || end > arr.length)
      throw new ArrayIndexOutOfBoundsException();
    this.arr = arr;
    this.start = start;
    this.end = end;
  }

  @Override
  public Byte at(final int i) {
    return arr[start + i];
  }

  @Override
  public int length() {
    return end - start;
  }

  @Override
  public ByteSeq sub(final int start, final int end) {
    return new ByteSeq(arr, start + this.start, end+ this.start);
  }

  @Override
  public boolean isImmutable() {
    return true;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ByteSeq)) {
      return false;
    }

    final ByteSeq intSeq = (ByteSeq) o;

    if (end != intSeq.end) {
      return false;
    }
    if (start != intSeq.start) {
      return false;
    }
    return Arrays.equals(arr, intSeq.arr);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(arr);
    result = 31 * result + start;
    result = 31 * result + end;
    return result;
  }

  @Override
  public Class<Byte> elementType() {
    return byte.class;
  }

  public byte byteAt(final int index) {
    return arr[start + index];
  }
}
