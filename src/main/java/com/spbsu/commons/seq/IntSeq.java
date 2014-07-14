package com.spbsu.commons.seq;

/**
 * User: solar
 * Date: 08.07.14
 * Time: 11:07
 */
public class IntSeq extends Seq.Stub<Integer> {
  public final int[] arr;
  public final int start;
  public final int end;

  public IntSeq(final int[] arr) {
    this(arr, 0, arr.length);
  }

  public IntSeq(final int[] arr, int start, int end) {
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
    return new IntSeq(arr, start + this.start, end+ this.end);
  }

  @Override
  public boolean isImmutable() {
    return true;
  }
}
