package com.spbsu.commons.seq;

import org.jetbrains.annotations.NotNull;

/**
 * User: Igor Kuralenok
 * Date: 10.05.2006
 * Time: 17:55:23
 */
public class CharSeqChar extends CharSeq {
  public final char ch;

  protected CharSeqChar(final char ch) {
    this.ch = ch;
  }

  public char charAt(int offset) {
    if (offset != 0)
      throw new ArrayIndexOutOfBoundsException();
    return ch;
  }

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

  public char[] toCharArray() {
    final char[] chars = new char[length()];
    copyToArray(0, chars, 0, length());
    return chars;
  }

  public void copyToArray(int start, char[] array, int offset, int length) {
    if (length == 1 && start == 0)
      array[offset] = ch;
    else if (length > 0)
      throw new ArrayIndexOutOfBoundsException();
  }

  public CharSequence trim() {
    return CharSeqTools.isWhitespace(ch) ? EMPTY : this;
  }
}
