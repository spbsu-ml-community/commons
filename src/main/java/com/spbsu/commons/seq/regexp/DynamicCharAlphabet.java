package com.spbsu.commons.seq.regexp;

import com.spbsu.commons.seq.CharSeq;
import com.spbsu.commons.seq.Seq;
import com.spbsu.commons.util.ArrayTools;

import java.util.Arrays;

/**
 * User: solar
 * Date: 20.09.17
 */
public class DynamicCharAlphabet implements Alphabet<Character> {
  private char[] chars;
  private int[] ichars = new int[Character.MAX_VALUE + 1];

  {
    Arrays.fill(ichars, -1);
  }

  public DynamicCharAlphabet(char[] chars) {
    this.chars = chars.clone();
    for (int i = 0; i < chars.length; i++) {
      ichars[chars[i]] = i;
    }
  }

  public DynamicCharAlphabet() {
    chars = new char[0];
  }

  @Override
  public Character getT(Matcher.Condition condition) {
    return condition instanceof CharCondition ? ((CharCondition) condition).ch : null;
  }

  @Override
  public Matcher.Condition<Character> getByT(Character i) {
    if (ichars[i] < 0)
      expand(i);
    return new CharCondition(i);
  }

  @Override
  public Matcher.Condition<Character> get(int i) {
    try {
      return new CharCondition(chars[i]);
    }
    catch (ArrayIndexOutOfBoundsException aioobe) {
      return null;
    }
  }

  @Override
  public int getOrder(Matcher.Condition<Character> c) {
    return c instanceof CharCondition ? ichars[(int)((CharCondition) c).ch] : -1;
  }

  @Override
  public int index(Seq<Character> seq, int index) {
    if (seq instanceof CharSeq) {
      final char ch = ((CharSeq) seq).charAt(index);
      final int idx = ichars[ch];
      return idx < 0 ? expand(ch) : idx;
    }
    return index(seq.at(index));
  }

  @Override
  public int index(Character character) {
    final char ch = character;
    final int idx = ichars[ch];
    return idx < 0 ? expand(ch) : idx;
  }

  @Override
  public int size() {
    return chars.length;
  }

  protected int expand(char i) {
    final int index = this.chars.length;
    final char[] chars = new char[index + 1];
    System.arraycopy(this.chars, 0, chars, 0, index);
    chars[index] = i;
    ichars[i] = index;
    this.chars = chars;
    return index;
  }

  public char[] chars() {
    return chars;
  }

  public static class CharCondition implements Matcher.Condition<Character> {
    private char ch;

    public CharCondition(char ch) {
      this.ch = ch;
    }

    @Override
    public boolean is(Character frag) {
      return frag == ch;
    }
  }
}