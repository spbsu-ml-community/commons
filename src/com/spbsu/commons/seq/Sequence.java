package com.spbsu.commons.seq;

import java.util.AbstractList;
import java.util.List;

/**
 * User: Manokk
 * Date: 31.08.11
 * Time: 2:45
 */
public abstract class Sequence<T> extends AbstractList<T> {
  public static class CharSequence extends Sequence<Character> {
    char[] data;
    private int start;
    private int length;

    public CharSequence(char[] data, int start, int length) {
      this.data = data;
      this.start = start;
      this.length = length;
    }

    @Override
    public Character get(int i) {
      return data[start + i];
    }

    public char getFast(int i) {
      return data[start + i];
    }

    @Override
    public List<Character> subList(int start, int end) {
      return new CharSequence(data, start + this.start, end - start);
    }

    @Override
    public int size() {
      return length;
    }
    @Override
    public String toString() {
      return new String(data, start, length);
    }
  }
  
  public static Sequence<Character> charSequence(final String s) {
    char[] data = new char[s.length()];
    s.getChars(0, s.length(), data, 0);
    return new CharSequence(data, 0, s.length());
  }
}