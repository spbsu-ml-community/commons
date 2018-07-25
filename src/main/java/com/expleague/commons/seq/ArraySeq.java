package com.expleague.commons.seq;



import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * User: solar
 * Date: 07.07.14
 * Time: 22:49
 */
public class ArraySeq<T> extends Seq.Stub<T> {
  private final T[] arr;
  private final int start;
  private final int end;

  public ArraySeq(final T[] arr) {
    this(arr, 0, arr.length);
  }

  public ArraySeq(final T[] arr, final int start, final int end) {
    if (end > arr.length || start < 0)
      throw new ArrayIndexOutOfBoundsException();
    this.arr = arr;
    this.start = start;
    this.end = end;
  }

  public ArraySeq(final ArraySeq<T> arr, final int start, final int end) {
    if (end > arr.length() || start < 0)
      throw new ArrayIndexOutOfBoundsException();
    this.arr = arr.arr;
    this.start = arr.start + start;
    this.end = arr.start + end;
  }

  public ArraySeq(final Seq<T> arr, final int start, final int end) {
    if (end > arr.length() || start < 0)
      throw new ArrayIndexOutOfBoundsException();
    this.arr = arr.sub(start, end).toArray();
    this.start = 0;
    this.end = end - start;
  }

  public static <S> Seq<S> iterate(Class<S> cls, S element, int times) {
    final SeqBuilder<S> seqBuilder = new ArraySeqBuilder<>(cls);
    IntStream.range(0, times).forEach(i -> seqBuilder.add(element));
    return seqBuilder.build();
  }

  public static <S> Seq<S> emptySeq(Class<S> cls) {
    final SeqBuilder<S> seqBuilder = new ArraySeqBuilder<>(cls);
    return seqBuilder.build();
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
  public Seq<T> sub(int[] indices) {
    return IntStream.of(indices).mapToObj(this::at).collect(SeqTools.collect((Class<T>)elementType())).build();
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
  public Stream<T> stream() {
    if (start == 0 && end == arr.length)
      return Stream.of(arr);
    return IntStream.range(start, end).mapToObj(i -> arr[i]);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this)
      return true;
    if (obj == null || obj.getClass() != getClass())
      return false;

    final ArraySeq other = (ArraySeq) obj;
    return new EqualsBuilder().
        append(arr, other.arr).
        append(start, other.start).
        append(end, other.end).
        isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(arr).append(start).append(end).toHashCode();
  }
}
