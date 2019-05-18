package com.expleague.commons.seq;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * User: solar
 * Date: 08.07.14
 * Time: 11:07
 */
public class FloatSeq extends Seq.Stub<Float> {
  public static final FloatSeq EMPTY = new FloatSeq();
  public final float[] arr;
  public final int start;
  public final int end;


  public FloatSeq(final int dim) {
    arr = new float[dim];
    start = 0;
    end = dim;
  }

  public FloatSeq(final float... arr) {
    this(arr, 0, arr.length);
  }

  public FloatSeq(final float[] arr, final int start, final int end) {
    if (start < 0 || end > arr.length)
      throw new ArrayIndexOutOfBoundsException();
    this.arr = arr;
    this.start = start;
    this.end = end;
  }

  public static FloatSeq empty() {
    return EMPTY;
  }

  @Override
  public Float at(final int i) {
    return arr[start + i];
  }

  @Override
  public int length() {
    return end - start;
  }

  @Override
  public FloatSeq sub(final int start, final int end) {
    return new FloatSeq(arr, start + this.start, end + this.start);
  }

  @Override
  public FloatSeq sub(int[] indices) {
    final float[] result = new float[indices.length];
    IntStream.of(indices).forEach(idx -> result[idx] = arr[start + indices[idx]]);
    return new FloatSeq(result);
  }

  public int seek(int x) {
    return Arrays.binarySearch(arr, start, end, x);
  }

  @Override
  public float[] toArray() {
    if (start == 0 && end == arr.length)
      return arr;
    final float[] result = new float[length()];
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
    if (!(o instanceof FloatSeq)) {
      return false;
    }

    final FloatSeq intSeq = (FloatSeq) o;

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
  public Class<Float> elementType() {
    return float.class;
  }

  @Override
  public String toString() {
    return Arrays.toString(arr);
  }

  public float floatAt(final int index) {
    //    if (index >= arr.length)
    //      throw new ArrayIndexOutOfBoundsException();
    return arr[start + index];
  }

  public void adjust(int index, float adj) {
    arr[start + index] += adj;
  }

  @SuppressWarnings("unchecked")
  public DoubleStream stream() {
    return IntStream.range(start, end).mapToDouble(idx -> arr[idx]);
  }
}
