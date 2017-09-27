package com.spbsu.commons.seq;

/**
 * User: Igor Kuralenok
 * Date: 10.05.2006
 * Time: 17:55:23
 */
public class CharSeqChar extends CharSeq {
  public final char ch;

  public CharSeqChar(final char ch) {
    this.ch = ch;
  }

  @Override
  public char charAt(final int offset) {
    if (offset != 0)
      throw new ArrayIndexOutOfBoundsException();
    return ch;
  }

  @Override
  public CharSeq sub(final int start, final int end){
    if (end - start == 0)
      return EMPTY;
    if (end == 1 && start == 0)
      return this;
    throw new ArrayIndexOutOfBoundsException();
  }

  @Override
  public int length() {
    return 1;
  }

  @Override
  public char[] toCharArray() {
    final char[] chars = new char[length()];
    copyToArray(0, chars, 0, length());
    return chars;
  }

  @Override
  public void copyToArray(final int start, final char[] array, final int offset, final int length) {
    if (length == 1 && start == 0)
      array[offset] = ch;
    else if (length > 0)
      throw new ArrayIndexOutOfBoundsException();
  }

  @Override
  public CharSeq trim() {
    return CharSeqTools.isWhitespace(ch) ? EMPTY : this;
  }
}
