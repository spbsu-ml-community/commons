package com.spbsu.commons.seq;


import gnu.trove.strategy.HashingStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * User: terry
 * Date: 10.10.2009
 * Time: 23:26:28
 */
public class CharSeqTools {
  public static final String EMPTY = "";
  public static final HashingStrategy<CharSequence> STRATEGY = new HashingStrategy<CharSequence>() {
    public int computeHashCode(final CharSequence cs) {
      return cs.hashCode();
    }

    public boolean equals(final CharSequence cs1, final CharSequence cs2) {
      return CharSeqTools.equals(cs1, cs2);
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
    return new CharSeqArray(result, 0, result.length);
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
    return new CharSeqArray(result, 0, result.length);
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

  public static CharSequence[] split(CharSequence sequence, CharSequence separator) {
    final List<CharSequence> result = new ArrayList<CharSequence>(10);
    int last = 0;
    for (int i = 0; i < sequence.length(); i++) {
      boolean accept = separator.length() <= sequence.length() - i;
      for (int j = 0; j < separator.length() && accept; j++) { // need to change to something faster
        if (sequence.charAt(i + j) != separator.charAt(j))
          accept = false;
      }
      if (accept) {
        result.add(sequence.subSequence(last, i));
        last = i + separator.length();
      }
    }
    result.add(sequence.subSequence(last, sequence.length()));
    return result.toArray(new CharSequence[result.size()]);
  }

  public static CharSequence cut(CharSequence from, int index, char sep) {
    final int start = index;
    while (from.length() > index && from.charAt(index) != sep)
      index++;
    return from.subSequence(start, index);
  }

  public static CharSequence cutBetween(CharSequence sequence, int startIndex, char fromSymbol, char toSymbol) {
    final int startPos = skipTo(sequence, startIndex, fromSymbol) + 1;
    if (startPos >= sequence.length()) {
      return EMPTY;
    }
    int index = startPos;
    int depth = 0;
    while (index < sequence.length()) {
      char currentSymbol = sequence.charAt(index);
      if (currentSymbol == fromSymbol) {
        depth++;
      } else if (currentSymbol == toSymbol) {
        if (depth == 0) {
          break;
        } else {
          depth--;
        }
      }
      index++;
    }
    return sequence.subSequence(startPos, index);
  }

  public static int skipTo(CharSequence from, int index, char sep) {
    while (from.length() > index && from.charAt(index) != sep)
      index++;
    return index;
  }

  public static boolean startsWith(CharSequence seq, CharSequence prefix) {
    if (seq.length() < prefix.length())
      return false;
    int index = 0;
    while(index < prefix.length()) {
      if(prefix.charAt(index) != seq.charAt(index))
        return false;
      index++;
    }

    return true;
  }
  public static class LexicographicalComparator implements Comparator<CharSequence> {
    public int compare(CharSequence a, CharSequence b) {
      int index = 0;
      while (a.length() > index && b.length() > index) {
        char aCh = a.charAt(index);
        char bCh = b.charAt(index);
        if (aCh != bCh)
          return aCh - bCh;
        index++;
      }
      return Integer.compare(a.length(), b.length());
    }
  }
}
