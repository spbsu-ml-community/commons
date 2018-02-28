package com.expleague.commons.seq;

public class CharSeqByteArray extends CharSeq {
  public int baseChar;
  public final byte[] array;
  public final int start;
  public final int end;

  public CharSeqByteArray(int baseChar, final byte[] array, final int start, final int end) {
    if (end < start)
      throw new ArrayIndexOutOfBoundsException();
    this.baseChar = baseChar;
    this.array = array;
    this.start = start;
    this.end = end;
  }

  @Override
  public int length() {
    return end - start;
  }

  @Override
  public char charAt(final int offset) {
    return (char)(baseChar + array[start + offset]);
  }

  @Override
  public CharSeq sub(final int start, final int end) {
    return new CharSeqByteArray(baseChar, array, start + this.start, end + this.start);
  }

  @Override
  public void copyToArray(final int start, final char[] array, final int offset, final int length) {
    for (int i = 0; i < length; i++) {
      array[offset + i] = (char) (baseChar + this.array[this.start + start + i]);
    }
  }
}
