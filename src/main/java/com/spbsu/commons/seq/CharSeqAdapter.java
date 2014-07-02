package com.spbsu.commons.seq;

/**
 * User: solar
 * Date: 02.07.14
 * Time: 10:48
 */
public class CharSeqAdapter extends CharSeq {
  private final CharSequence delegate;

  public CharSeqAdapter(final CharSequence delegate) {
    this.delegate = delegate;
  }

  @Override
  public char charAt(final int offset) {
    return delegate.charAt(offset);
  }

  @Override
  public CharSeq sub(final int start, final int end) {
    return new CharSeqAdapter(delegate.subSequence(start, end));
  }

  @Override
  public int length() {
    return delegate.length();
  }
}
