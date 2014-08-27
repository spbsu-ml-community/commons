package com.spbsu.commons.seq;

/**
 * User: solar
 * Date: 02.07.14
 * Time: 10:48
 */
public class CharSeqAdapter extends CharSeq {
  private final int start, end;
  private final CharSequence delegate;

  public CharSeqAdapter(final CharSequence delegate) {
    this(delegate, 0, delegate.length());
  }
  public CharSeqAdapter(final CharSequence delegate, int start, int end) {
    if (start < 0 || start > end || end > delegate.length())
      throw new ArrayIndexOutOfBoundsException();
    this.start = start;
    this.end = end;
    this.delegate = delegate;
  }

  @Override
  public char charAt(final int offset) {
    return delegate.charAt(start + offset);
  }

  @Override
  public CharSeq sub(final int start, final int end) {
    return new CharSeqAdapter(delegate, start + this.start, end + this.start);
  }

  @Override
  public int length() {
    return end - start;
  }
}
