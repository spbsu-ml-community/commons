package com.spbsu.commons.seq;

import org.jetbrains.annotations.NotNull;

/**
 * User: Igor Kuralenok
 * Date: 10.05.2006
 * Time: 17:55:23
 */
public abstract class CharSeq extends Sequence<Character> implements CharSequence {
  public static final CharSeq EMPTY = new CharSeqArray(new char[]{}, 0, 0);

  public abstract char charAt(int offset);
  public abstract CharSeq sub(final int start, final int end);

  public final CharSeq subSequence(int start, int end) {
    return sub(start, end);
  }

  public final CharSeq subSequence(int start) {
    return subSequence(start, length());
  }

  @Override
  public final Character at(final int i) {
    return charAt(i);
  }


  public boolean equals(final Object object) {
    if (object instanceof CharSequence) {
      final CharSequence str = (CharSequence) object;
      final int length = length();
      if (str.length() != length) {
        return false;
      }
      if (str.hashCode() != hashCode()) {
        return false;
      }
      int index = 0;
      while (index < length) {
        if (str.charAt(index) != charAt(index)) {
          return false;
        }
        index++;
      }
      return true;
    }
    return false;
  }

  @NotNull
  public String toString() {
    return new String(toCharArray());
  }

  protected int hashCode;

  public int hashCode() {
    if (hashCode != 0) {
      return hashCode;
    }

    int len = length();
    int h = 0;
    for (int i = 0; i < len; i++) {
      h = 31 * h + charAt(i);
    }
    return hashCode = h;
  }

  public char[] toCharArray() {
    final char[] chars = new char[length()];
    copyToArray(0, chars, 0, length());
    return chars;
  }

  public void copyToArray(int start, char[] array, int offset, int length) {
    int index = 0;
    while (index++ < length) {
      array[offset++] = charAt(start++);
    }
  }

  public CharSequence trim() {
    int nonWsStart = 0;
    final int length = length();
    int nonWsEnd = length;
    //noinspection StatementWithEmptyBody
    while (nonWsStart < length && CharSeqTools.isWhitespace(charAt(nonWsStart++))) ;
    //noinspection StatementWithEmptyBody
    while (nonWsEnd >= 0 && CharSeqTools.isWhitespace(charAt(--nonWsEnd))) ;

    return subSequence(nonWsStart - 1, nonWsEnd + 1);
  }

  public static CharSequence createArrayBasedSequence(final CharSequence text) {
    if (text instanceof CharSeqArray) {
      return text;
    }
    return allocateArrayBasedSequence(text);
  }

  public static CharSequence allocateArrayBasedSequence(final CharSequence text) {
    final int textLen = text.length();
    final char[] chars = new char[textLen];
    int index = 0;
    while (index < textLen) {
      chars[index] = text.charAt(index++);
    }
    return create(chars);
  }

  public static CharSeq create(char[] text, int start, int end) {
    return new CharSeqArray(text, start, end);
  }

  public static CharSeq create(char[] text) {
    return new CharSeqArray(text, 0, text.length);
  }
}
