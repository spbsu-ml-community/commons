package com.expleague.commons.seq;

/**
 * User: solar
 * Date: 02.07.14
 * Time: 10:48
 */
public class CharSeqAdapter extends CharSeq {
  private final int start, end;
  private final CharSequence delegate;

  CharSeqAdapter(final CharSequence delegate) {
    this(delegate, 0, delegate.length());
  }
  CharSeqAdapter(final CharSequence delegate, final int start, final int end) {
    if (delegate instanceof CharSeq)
      throw new IllegalArgumentException();
    if (start < 0 || start > end || end > delegate.length())
      throw new ArrayIndexOutOfBoundsException();
    this.start = start;
    this.end = end;
    this.delegate = delegate;
  }

  @Override
  public final char charAt(final int offset) {
    return delegate.charAt(start + offset);
  }

  @Override
  public CharSeq sub(final int start, final int end) {
    return new CharSeqAdapter(delegate, start + this.start, end + this.start);
  }

  @Override
  public void copyToArray(int start, char[] array, int offset, int length) {
    start += this.start;
    if (delegate instanceof String)
      ((String) delegate).getChars(start, start + length, array, offset);
    else if (delegate instanceof CharSeq)
      ((CharSeq) delegate).copyToArray(start, array, offset, length);
    else
      super.copyToArray(start, array, offset, length);
  }

  @Override
  public int length() {
    return end - start;
  }

  @Override
  public boolean isImmutable() {
    if (delegate instanceof String)
      return true;
    else if (delegate instanceof CharSeq)
      return ((CharSeq) delegate).isImmutable();
    return false;
  }

  public CharSequence original() {
    return delegate;
  }

  public static CharSeq create(CharSequence seq) {
    if (seq instanceof CharSeq)
      return (CharSeq)seq;
    return new CharSeqAdapter(seq);
  }

  public static CharSeq create(CharSequence seq, int start, int end) {
    if (seq instanceof CharSeq)
      return ((CharSeq)seq).sub(start, end);
    return new CharSeqAdapter(seq, start, end);
  }
}
