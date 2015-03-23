package com.spbsu.commons.seq;

import java.util.Arrays;

/**
 * User: solar
 * Date: 08.07.14
 * Time: 11:07
 */
public class IntSeq extends Seq.Stub<Integer> {
  public final int[] arr;
  public final int start;
  public final int end;

  public IntSeq(final int... arr) {
    this(arr, 0, arr.length);
  }

  public IntSeq(final int[] arr, final int start, final int end) {
    if (start < 0 || end > arr.length)
      throw new ArrayIndexOutOfBoundsException();
    this.arr = arr;
    this.start = start;
    this.end = end;
  }

  @Override
  public Integer at(final int i) {
    return arr[start + i];
  }

  @Override
  public int length() {
    return end - start;
  }

  @Override
  public IntSeq sub(final int start, final int end) {
    return new IntSeq(arr, start + this.start, end+ this.start);
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
    if (!(o instanceof IntSeq)) {
      return false;
    }

    final IntSeq intSeq = (IntSeq) o;

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
  public Class<Integer> elementType() {
    return int.class;
  }

  public int intAt(final int index) {
    return arr[start + index];
  }
}
