package com.expleague.commons.seq.regexp;

import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.seq.Seq;

/**
 * Created by IntelliJ IDEA.
 * User: Manokk
 * Date: 30.08.11
 * Time: 11:17
 */
public interface Alphabet<T> {

  int size();

  int indexCondition(Matcher.Condition<T> c);

  Matcher.Condition<T> condition(int i);

  Matcher.Condition<T> conditionByT(T i);

  T getT(Matcher.Condition<T> condition);

  int index(T t);

  default int index(Seq<T> seq, int index) {
    return index(seq.at(index));
  }

  Alphabet<Character> CHARACTER_ALPHABET = new Alphabet<Character>() {
    private static final int ALPHABET_SIZE = 'z' - 'a' + 1;

    class CharCondition implements Matcher.Condition<Character> {
      private final Character my;

      public CharCondition(final char c) {
        my = c;
      }

      @Override
      public boolean is(final Character frag) {
        return frag == my;
      }

      @Override
      public boolean equals(final Object o) {
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

    @Override
    public int size() {
      return ALPHABET_SIZE;
    }

    @Override
    public int indexCondition(final Matcher.Condition<Character> c) {
      if (c instanceof CharCondition) {
        final CharCondition condition = (CharCondition) c;
        return condition.my - 'a';
      } else if (c == SimpleRegExp.Condition.ANY)
        return ALPHABET_SIZE;
      throw new IllegalArgumentException("Not a char condition");
    }

    @Override
    public SimpleRegExp.Condition<Character> condition(final int i) {
      if (i < ALPHABET_SIZE)
        return new CharCondition((char) ('a' + i));
      if (i == ALPHABET_SIZE)
        //noinspection unchecked
        return SimpleRegExp.Condition.ANY;
      throw new IllegalArgumentException("Condition is not in range");
    }

    @Override
    public SimpleRegExp.Condition<Character> conditionByT(final Character ch) {
      return condition(ch - 'a');
    }

    @Override
    public Character getT(final Matcher.Condition<Character> condition) {
      if (condition instanceof CharCondition)
        return ((CharCondition)condition).my;
      else if (condition == SimpleRegExp.Condition.ANY)
        return '.';
      return null;
    }

    @Override
    public int index(final Character character) {
      return character - 'a';
    }

    @Override
    public int index(final Seq<Character> characters, final int index) {
      return ((CharSeq)characters).charAt(index) - 'a';
    }
  };

  default IntSeq reindex(Seq<T> seq) {
    final int[] result = new int[seq.length()];
    for (int i = 0; i < result.length; i++) {
      result[i] = index(seq.at(i));
    }
    return new IntSeq(result);
  }
}