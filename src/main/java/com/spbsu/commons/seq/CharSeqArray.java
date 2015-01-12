package com.spbsu.commons.seq;

import org.jetbrains.annotations.NotNull;

public class CharSeqArray extends CharSeq {
  public final char[] array;
  public final int start;
  public final int end;

  public CharSeqArray(final char[] array, final int start, final int end) {
    this.array = array;
    this.start = start;
    this.end = end;
  }

  public CharSeqArray(final char[] chars) {
    this(chars, 0, chars.length);
  }

  @Override
  public int length() {
    return end - start;
  }
  @Override
  public char charAt(final int offset) {
    return array[start + offset];
  }

  @Override
  public CharSeq sub(final int start, final int end) {
    return new CharSeqArray(array, start + this.start, end + this.start);
  }

  @NotNull
  public String toString() {
    return new String(array, start, end - start);
  }

  @Override
  public char[] toCharArray() {
    if(start == 0 && end == array.length) {
      return array;
    }
    return super.toCharArray();
  }

  @Override
  public void copyToArray(final int start, final char[] array, final int offset, final int length) {
    System.arraycopy(this.array, this.start + start, array, offset, length);
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) return true;
    if (object instanceof CharSeqArray) {
      final CharSeqArray other = (CharSeqArray) object;
      if (other.hashCode != 0 && this.hashCode != 0 && other.hashCode != this.hashCode) return false;
      
      final char[] otherArray = other.array;
      final char[] thisArray = this.array;
      final int otherStart = other.start;
      final int thisStart = this.start;
      final int otherEnd = other.end;
      final int thisEnd = this.end;

      if (otherArray == thisArray && otherStart == thisStart && otherEnd == thisEnd) {
        return true;
      }

      final int thisLength = thisEnd - thisStart;
      final int otherLength = otherEnd - otherStart;
      if (otherLength != thisLength) return false;

      for (int i = 0; i < thisLength; i++) {
        if (otherArray[otherStart + i] != thisArray[thisStart + i]) return false;
      }

      return true;
    }
    return super.equals(object);
  }

  protected int hashCode;
  public int hashCode() {
    if(hashCode != 0) {
      return hashCode;
    }
    int h = 0;
    for (int i = start; i < end; i++) {
      h = 31*h + array[i];
    }
    return hashCode = h;
  }
}
