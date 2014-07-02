package com.spbsu.commons.seq;

/**
 * User: Manokk
 * Date: 31.08.11
 * Time: 2:45
 */
public abstract class Sequence<T> {
  public abstract T at(int i);
  public abstract Sequence<T> sub(int start, int end);
  public abstract int length();

  public static Sequence<Character> charSequence(final String s) {
    char[] data = new char[s.length()];
    s.getChars(0, s.length(), data, 0);
    return new CharSeqArray(data, 0, s.length());
  }
}