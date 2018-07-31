package com.expleague.commons.seq;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * User: solar
 * Date: 08.07.14
 * Time: 11:07
 */
public class IntSeq extends Seq.Stub<Integer> {
  public static final IntSeq EMPTY = new IntSeq(0);
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
  public Seq<Integer> sub(int[] indices) {
    return new IntSeq(IntStream.of(indices).map(idx -> arr[start + idx]).toArray());
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

  public IntStream stream() {
    return StreamSupport.intStream(new Spliterators.AbstractIntSpliterator(length(), Spliterator.IMMUTABLE) {
      int cursor = 0;
      @Override
      public boolean tryAdvance(IntConsumer action) {
        if (cursor < length()) {
          action.accept(intAt(cursor++));
          return true;
        }
        return false;
      }
    }, false);
  }
}
