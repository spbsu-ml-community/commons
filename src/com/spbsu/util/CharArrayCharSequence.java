package com.spbsu.util;

public class CharArrayCharSequence extends CharSequenceBase {
  public final char[] array;
  public final int start;
  public final int end;

  public CharArrayCharSequence(final char[] array, final int start, final int end) {
    this.array = array;
    this.start = start;
    this.end = end;
  }

  public int length() {
    return end - start;
  }

  public char charAt(final int offset) {
    return array[start + offset];
  }

  public String toString() {
    return new String(array, start, end - start);
  }

  public CharSequence subSequence(final int start, final int end) {
    return new CharArrayCharSequence(array, this.start + start, this.start + end);
  }

  public char[] toCharArray() {
    if(start == 0 && end == array.length) return array;
    return super.toCharArray();
  }

  public void copyToArray(final int start, final char[] array, final int offset, final int length) {
    System.arraycopy(this.array, this.start + start, array, offset, length);
  }

  public int hashCode() {
    if(hashCode != 0) return hashCode;
    int h = 0;
    for (int i = start; i < end; i++) {
      h = 31*h + array[i];
    }
    return hashCode = h;
  }

  public int getStartOffset() {
    return start;
  }
}
