package com.spbsu.util;

import java.io.Serializable;

/**
 * User: Igor Kuralenok
 * Date: 10.05.2006
 */
public abstract class CharSequenceBase implements CharSequence, Serializable {
  public static final CharSequence EMPTY = "";

  public static CharSequence create(char[] text, int start, int end) {
    return new CharArrayCharSequence(text, start, end);
  }

  public static CharSequence create(char[] text) {
    return new CharArrayCharSequence(text, 0, text.length);
  }

  public abstract int length();

  public abstract char charAt(int offset);

  public abstract CharSequence subSequence(int start, int end);

  public CharSequence subSequence(int start) {
    return subSequence(start, length());
  }

  public boolean equals(final Object object) {
    if (object == this) return true;

    if(object instanceof CharSequence){
      final CharSequence str = (CharSequence) object;
      final int length = length();
      if(str.length() != length) return false;
      if(str.hashCode() != hashCode()) return false;
      int index = 0;
      while(index < length){
        if(str.charAt(index) != charAt(index)) return false;
        index++;
      }
      return true;
    }
    return false;
  }

  public String toString(){
    return new String(toCharArray());
  }

  protected int hashCode;

  public int hashCode() {
    if(hashCode != 0) return hashCode;

    int len = length();
    int h = 0;
    for (int i = 0; i < len; i++) {
      h = 31*h + charAt(i);
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
    while(index++ < length) {
      array[offset++] = charAt(start++);
    }
  }

  public CharSequence trim() {
    int nonWsStart = 0;
    final int length = length();
    int nonWsEnd = length;
    //noinspection StatementWithEmptyBody
    while (nonWsStart < length && TextUtil.isWhitespace(charAt(nonWsStart++)));
    //noinspection StatementWithEmptyBody
    while (nonWsEnd >= 0 && TextUtil.isWhitespace(charAt(--nonWsEnd)));

    return subSequence(nonWsStart - 1, nonWsEnd + 1);
  }

  public static CharSequence createArrayBasedSequence(CharSequence text) {
    if(text instanceof CharArrayCharSequence) return text;
    final int textLen = text.length();
    final char[] chars = new char[textLen];
    int index = 0;
    while(index < textLen) chars[index] = text.charAt(index++);
    return create(chars);
  }
}
