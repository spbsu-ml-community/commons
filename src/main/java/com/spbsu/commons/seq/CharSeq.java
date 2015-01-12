package com.spbsu.commons.seq;

import org.jetbrains.annotations.NotNull;

/**
 * User: Igor Kuralenok
 * Date: 10.05.2006
 * Time: 17:55:23
 */
public abstract class CharSeq implements Seq<Character>, CharSequence {
  public static final CharSeq EMPTY = new CharSeqArray(new char[]{}, 0, 0);

  public abstract char charAt(int offset);
  public abstract CharSeq sub(final int start, final int end);

  @Override
  public boolean isImmutable() {
    return true;
  }

  public final CharSeq subSequence(final int start, final int end) {
    return sub(start, end);
  }

  public final CharSeq subSequence(final int start) {
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

  public int hashCode() {
    final int len = length();
    int h = 0;
    for (int i = 0; i < len; i++) {
      h = 31 * h + charAt(i);
    }
    return h == 0 ? 1 : h;
  }

  public char[] toCharArray() {
    final char[] chars = new char[length()];
    copyToArray(0, chars, 0, length());
    return chars;
  }

  public void copyToArray(int start, final char[] array, int offset, final int length) {
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

  public static CharSeq create(final char[] text, final int start, final int end) {
    return new CharSeqArray(text, start, end);
  }

  public static CharSeq create(final char[] text) {
    return new CharSeqArray(text, 0, text.length);
  }

  public static CharSeq copy(final char[] text) {
    return copy(text, 0, text.length);
  }

  public static CharSeq copy(final CharSequence text) {
    return copy(text, 0, text.length());
  }

  public static CharSeq copy(final CharSequence text, final int start, final int end) {
    final char[] copy = new char[end - start];
    if (text instanceof CharSeq) {
      ((CharSeq) text).copyToArray(start, copy, 0, end - start);
    }
    else {
      for (int i = 0; i < copy.length; i++) {
        copy[i] = text.charAt(i);
      }
    }
    return new CharSeqArray(copy);
  }

  public static CharSeq copy(final char[] text, final int start, final int end) {
    final char[] copy = new char[end - start];
    System.arraycopy(text, start, copy, 0, end - start);
    return new CharSeqArray(copy);
  }

  public static CharSeq create(final CharSequence string) {
    return new CharSeqAdapter(string);
  }

  @Override
  public Class<Character> elementType() {
    return char.class;
  }

  public int indexOf(final char ch) {
    int index = 0;
    while (index < length() && at(index) != ch)
      index++;
    return index;
  }
}
