package com.expleague.commons.seq;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * User: solar
 * Date: 08.07.14
 * Time: 11:07
 */
public class LongSeq extends Seq.Stub<Long> {
  public static final LongSeq EMPTY = new LongSeq();
  final long[] arr;
  final int start;
  private final int end;

  public LongSeq(final long... arr) {
    this(arr, 0, arr.length);
  }

  public LongSeq(final long[] arr, final int start, final int end) {
    if (start < 0 || end > arr.length)
      throw new ArrayIndexOutOfBoundsException();
    this.arr = arr;
    this.start = start;
    this.end = end;
  }

  public static LongSeq empty() {
    return EMPTY;
  }

  @Override
  public Long at(final int i) {
    return arr[start + i];
  }

  @Override
  public int length() {
    return end - start;
  }

  @Override
  public LongSeq sub(final int start, final int end) {
    return new LongSeq(arr, start + this.start, end + this.start);
  }

  @Override
  public LongSeq sub(int[] indices) {
    return new LongSeq(IntStream.of(indices).mapToLong(idx -> arr[start + idx]).toArray());
  }

  public int seek(long x) {
    int optimismLimit = Math.min(start + 8, this.end); // single cache line read
    for (int i = start; i < optimismLimit; i++) {
      if (arr[i] >= x)
        return arr[i] == x ? i : -i - 1;
    }
    return Arrays.binarySearch(arr, optimismLimit, end, x);
  }

  @Override
  public long[] toArray() {
    if (start == 0 && end == arr.length)
      return arr;
    return Arrays.copyOfRange(arr, start, start + length());
  }

  public long[] data() {
    return arr;
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
    if (!(o instanceof LongSeq)) {
      return false;
    }

    final LongSeq intSeq = (LongSeq) o;

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
  public Class<Long> elementType() {
    return long.class;
  }

  @Override
  public String toString() {
    return Arrays.toString(arr);
  }

  public long longAt(final int index) {
    return arr[start + index];
  }

  @SuppressWarnings("unchecked")
  public LongStream stream() {
    return Arrays.stream(arr, start, end);
  }
}
