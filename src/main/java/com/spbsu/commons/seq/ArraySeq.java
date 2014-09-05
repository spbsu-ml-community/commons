package com.spbsu.commons.seq;

import java.lang.reflect.Array;

/**
 * User: solar
 * Date: 07.07.14
 * Time: 22:49
 */
public class ArraySeq<T> extends Seq.Stub<T> {
  private final T[] arr;
  private int start;
  private int end;

  public ArraySeq(final T[] arr) {
    this(arr, 0, arr.length);
  }

  public ArraySeq(final T[] arr, int start, int end) {
    if (end > arr.length || start < 0)
      throw new ArrayIndexOutOfBoundsException();
    this.arr = arr;
    this.start = start;
    this.end = end;
  }

  @Override
  public T at(final int i) {
    return arr[start + i];
  }

  @Override
  public Seq<T> sub(final int start, final int end) {
    if (end + this.start > this.end)
      throw new ArrayIndexOutOfBoundsException();
    return new ArraySeq<T>(arr, start + this.start, end + this.start);
  }

  @Override
  public int length() {
    return end - start;
  }

  @Override
  public boolean isImmutable() {
    return true;
  }

  @Override
  public Class<T> elementType() {
    //noinspection unchecked
    return (Class<T>)arr.getClass().getComponentType();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    for (int i = 0; i < this.length(); i++) {
      builder.append(this.at(i).toString());
    }
    return builder.toString();
  }
}
