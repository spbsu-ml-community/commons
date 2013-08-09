package com.spbsu.commons.text;

import gnu.trove.TObjectHashingStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: terry
 * Date: 10.10.2009
 * Time: 23:26:28
 */
public class CharSequenceTools {
  public static final String EMPTY = "";
  public static final TObjectHashingStrategy<CharSequence> STRATEGY = new TObjectHashingStrategy<CharSequence>() {
    public int computeHashCode(final CharSequence cs) {
      return cs.hashCode();
    }

    public boolean equals(final CharSequence cs1, final CharSequence cs2) {
      return CharSequenceTools.equals(cs1, cs2);
    }
  };
  
  public static boolean isWhitespace(char ch) {
    return ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r';
  }

  public static boolean equals(CharSequence text, CharSequence other) {
    if (text == other) {
      return true;
    }
    final int length = text.length();
    if (length != other.length()) {
      return false;
    }
    int index = 0;
    while (index < length) {
      if (text.charAt(index) != other.charAt(index)) {
        return false;
      }
      index++;
    }
    return true;
  }

  public static boolean isNullOrEmpty(CharSequence charSequence) {
    return charSequence == null || charSequence.length() == 0;
  }

  public static CharSequence toLowerCase(CharSequence word) {
    char[] result = null;
    final int length = word.length();
    for (int i = 0; i < length; i++) {
      if (Character.isUpperCase(word.charAt(i))) {
        result = new char[length];
        break;
      }
    }
    if (result == null) {
      return word;
    }
    for (int i = 0; i < length; i++) {
      result[i] = Character.toLowerCase(word.charAt(i));
    }
    return new CharArrayCharSequence(result, 0, result.length);
  }

  public static CharSequence toUpperCase(CharSequence word) {
    char[] result = null;
    final int length = word.length();
    for (int i = 0; i < length; i++) {
      if (Character.isLowerCase(word.charAt(i))) {
        result = new char[length];
        break;
      }
    }
    if (result == null) {
      return word;
    }
    for (int i = 0; i < length; i++) {
      result[i] = Character.toUpperCase(word.charAt(i));
    }
    return new CharArrayCharSequence(result, 0, result.length);
  }

  public static CharSequence trim(final CharSequence word) {
    final int initialLength = word.length();
    int len = initialLength;
    int st = 0;

    while ((st < len) && (word.charAt(st) <= ' ')) {
      st++;
    }
    while ((st < len) && (word.charAt(len - 1) <= ' ')) {
      len--;
    }
    return ((st > 0) || (len < initialLength)) ? word.subSequence(st, len) : word;
  }

  public static String concatWithDelimeter(final CharSequence delimeter, final CharSequence... texts) {
    return concatWithDelimeter(delimeter, Arrays.asList(texts));
  }

  public static String concatWithDelimeter(final CharSequence delimeter, final List<? extends CharSequence> texts) {
    final StringBuilder result = new StringBuilder();
    for (int i = 0; i < texts.size() - 1; i++) {
      result.append(texts.get(i));
      result.append(delimeter);
    }

    if (texts.size() > 0) {
      result.append(texts.get(texts.size() - 1));
    }

    return result.toString();
  }

  public static String repeatWithDelimeter(final CharSequence delimeter, final CharSequence text, final int count) {
    final StringBuilder result = new StringBuilder();
    for (int i = 0; i < count - 1; i++) {
      result.append(text);
      result.append(delimeter);
    }

    if (count > 0) {
      result.append(text);
    }

    return result.toString();
  }

  public static String concat(final List<? extends CharSequence> seq) {
    int size = 0;
    for (final CharSequence cs : seq) {
      size += cs.length();
    }

    final StringBuilder result = new StringBuilder(size + 1);
    for (final CharSequence cs : seq) {
      result.append(cs);
    }

    return result.toString();
  }

  public static String concat(final CharSequence... texts) {
    return concat(Arrays.asList(texts));
  }

  public static CharSequence[] split(CharSequence sequence, char separator) {
    final List<CharSequence> result = new ArrayList<CharSequence>(10);
    int last = 0;
    for (int i = 0; i < sequence.length(); i++) {
      if (sequence.charAt(i) == separator) {
        result.add(sequence.subSequence(last, i));
        last = i + 1;
      }
    }
    result.add(sequence.subSequence(last, sequence.length()));
    return result.toArray(new CharSequence[result.size()]);
  }
}
