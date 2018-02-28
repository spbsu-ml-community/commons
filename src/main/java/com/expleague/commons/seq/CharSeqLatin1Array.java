package com.expleague.commons.seq;

public class CharSeqLatin1Array extends CharSeq {
  public final byte[] array;
  public final int start;
  public final int end;

  public CharSeqLatin1Array(final byte[] array, final int start, final int end) {
    if (end < start)
      throw new ArrayIndexOutOfBoundsException();
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
    int b = this.array[this.start + offset];
    return (char) (b >= 0 ? b : 256 + b);
  }

  @Override
  public CharSeq sub(final int start, final int end) {
    return new CharSeqLatin1Array(array, start + this.start, end + this.start);
  }

  @Override
  public void copyToArray(final int start, final char[] array, final int offset, final int length) {
    for (int i = 0; i < length; i++) {
      int b = this.array[this.start + start + i];
      array[offset + i] = (char) (b >= 0 ? b : 256 + b);
    }
  }
}
