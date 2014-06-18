package com.spbsu.commons.seq;

import com.spbsu.commons.regexp.Matcher;
import com.spbsu.commons.regexp.SimpleRegExp;

/**
 * Created by IntelliJ IDEA.
 * User: Manokk
 * Date: 30.08.11
 * Time: 11:17
 */
public interface Alphabet<T> {

  int size();

  int getOrder(Matcher.Condition<T> c);

  Matcher.Condition<T> get(int i);

  Matcher.Condition<T> getByT(T i);

  T getT(Matcher.Condition condition);

  int index(T t);

  int index(Sequence<T> sequence, int index);

  public static final Alphabet<Character> CHARACTER_ALPHABET = new Alphabet<Character>() {
    private static final int ALPHABET_SIZE = 'z' - 'a' + 1;

    class CharCondition implements Matcher.Condition<Character> {
      private Character my;

      public CharCondition(char c) {
        my = c;
      }

      public boolean is(Character frag) {
        return frag == my;
      }

      @Override
      public boolean equals(Object o) {
        return (o instanceof CharCondition && my == ((CharCondition) o).my);
      }

      @Override
      public int hashCode() {
        return 17 + my.hashCode();
      }

      @Override
      public String toString() {
        return my.toString();
      }
    }

    public int size() {
      return ALPHABET_SIZE;
    }

    public int getOrder(Matcher.Condition<Character> c) {
      if (c instanceof CharCondition) {
        final CharCondition condition = (CharCondition) c;
        return condition.my - 'a';
      } else if (c == SimpleRegExp.Condition.ANY)
        return ALPHABET_SIZE;
      throw new IllegalArgumentException("Not a char condition");
    }

    public SimpleRegExp.Condition<Character> get(int i) {
      if (i < ALPHABET_SIZE)
        return new CharCondition((char) ('a' + i));
      if (i == ALPHABET_SIZE)
        //noinspection unchecked
        return SimpleRegExp.Condition.ANY;
      throw new IllegalArgumentException("Condition is not in range");
    }

    public SimpleRegExp.Condition<Character> getByT(Character ch) {
      return get(ch - 'a');
    }

    @Override
    public Character getT(Matcher.Condition condition) {
      if (condition instanceof CharCondition)
        return ((CharCondition)condition).my;
      else if (condition == SimpleRegExp.Condition.ANY)
        return '.';
      return null;
    }

    @Override
    public int index(Character character) {
      return character - 'a';
    }

    @Override
    public int index(Sequence<Character> characters, int index) {
      return ((Sequence.CharSequence)characters).getFast(index) - 'a';
    }
  };
}