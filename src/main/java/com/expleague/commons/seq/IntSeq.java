package com.expleague.commons.seq;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * User: solar
 * Date: 08.07.14
 * Time: 11:07
 */
public class IntSeq extends Seq.Stub<Integer> {
  public static final IntSeq EMPTY = new IntSeq();
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

  public static IntSeq empty() {
    return EMPTY;
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
    return new IntSeq(arr, start + this.start, end + this.start);
  }

  @Override
  public IntSeq sub(int[] indices) {
    return new IntSeq(IntStream.of(indices).map(idx -> arr[start + idx]).toArray());
  }

  public int seek(int x) {
    return Arrays.binarySearch(arr, start, end, x);
  }

  public int last() {
    return arr[arr.length - 1];
  }

  @Override
  public int[] toArray() {
    if (start == 0 && end == arr.length)
      return arr;
    final int[] result = new int[length()];
    System.arraycopy(arr, start, result, 0, length());
    return result;
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

  @Override
  public String toString() {
    return Arrays.toString(arr);
  }

  public int intAt(final int index) {
    //    if (index >= arr.length)
    //      throw new ArrayIndexOutOfBoundsException();
    return arr[start + index];
  }

  @SuppressWarnings("unchecked")
  public IntStream stream() {
    return Arrays.stream(arr, start, end);
  }
}
