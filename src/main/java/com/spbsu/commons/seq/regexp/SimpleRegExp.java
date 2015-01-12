package com.spbsu.commons.seq.regexp;

import java.util.Arrays;


import com.spbsu.commons.seq.CharSeq;
import com.spbsu.commons.seq.Seq;

/**
 * User: solar
 * Date: 12.09.11
 * Time: 20:18
 */
public class SimpleRegExp implements Matcher<Character> {
  public static SimpleRegExp create(final String str) {
    final Pattern<Character> result = new Pattern<>(Alphabet.CHARACTER_ALPHABET);
    for (int i = 0; i < str.length(); i+=2) {
      final Condition chCondition = str.charAt(i) == '.' ? Condition.ANY : Alphabet.CHARACTER_ALPHABET.getByT(str.charAt(i));
      Pattern.Modifier mod = Pattern.Modifier.NONE;
      if (str.length() > i + 1) {
        switch(str.charAt(i + 1)) {
          case '*':
            mod = Pattern.Modifier.STAR;
            break;
          case '?':
            mod = Pattern.Modifier.QUESTION;
            break;
          default:
            i--;
        }
      }
      //noinspection unchecked
      result.add((Condition<Character>)chCondition, mod);
    }
    return new SimpleRegExp(result);
  }

  private final Pattern<Character> processedExpression;
  private final Pattern<Character> pattern;

  long[] limits = new long[50];
  final int[][] statesStore;

  public SimpleRegExp(final Pattern<Character> expression) {
    pattern = expression;
    processedExpression = new Pattern<>(expression.alphabet());
    int prevMod = 0;
    Condition<Character> prevCon = null;
    final int expressionSize = expression.size();
    for (int s = 0; s < expressionSize; s++) {
      final Pattern.Modifier mod = expression.modifier(s);
      final Condition<Character> con = expression.condition(s);
      final boolean simplify = (prevMod + mod.ordinal() >= 3) && con.equals(prevCon);
      if (!simplify || mod == Pattern.Modifier.STAR) {
        if (simplify)
          processedExpression.removeLast((prevMod == Pattern.Modifier.QUESTION.ordinal()) ? 2 : 1);
        processedExpression.add(con, mod);
        if (mod == Pattern.Modifier.QUESTION)
          processedExpression.add(con, Pattern.Modifier.EMPTY);
        prevCon = con;
        prevMod = mod.ordinal();
      }
    }
    statesStore = new int[2][processedExpression.size()];
  }

  @Override
  public void match(final Seq<Character> sequence, final MatchVisitor visitor) {
    final CharSeq seq = (CharSeq) sequence;
    final Pattern<Character> expression = processedExpression;
    int index = 0;
    final int seqSize = seq.length();
    if (seqSize == 0)
      return;
    final int[][] statesStoreLocal = statesStore;
    Arrays.fill(statesStoreLocal[0], seqSize);
    Arrays.fill(statesStoreLocal[1], seqSize);

    final long[] limits = this.limits;
    int relIndex = 0;
    long last = 0xFFFFFFFF00000000L;

    try {
    for (int i = 0; i < seqSize + 1; i++) {
      int lcount = relIndex;
      final char current = i < seqSize ? seq.charAt(i) : seq.charAt(i - 1);
      final int[] states = statesStoreLocal[index % 2];
      final int[] next = statesStoreLocal[(index + 1) % 2];
//      Arrays.fill(next, seqSize);
      if (states[0] >= seqSize)
        states[0] = index;

      final int length = states.length;
      for (int s = 0; s < length; s++) {
        if (states[s] < seqSize) {
          final boolean match = i < seqSize && expression.condition(s).is(current);
          switch (expression.modifier(s)) {
            case QUESTION:
              if (s >= length - 2) {
                if (match || states[s] < index) {
                   limits[lcount++] = (((long)states[s]) << 32) | (index + ((match) ? 1 : 0));
                }
              } else
                states[s + 2] = Math.min(states[s], states[s + 2]);

              if (match)
                next[s + 1] = Math.min(states[s], next[s + 1]);
//              next[s] = seqSize;
              break;
            case EMPTY:
              if (s < length - 1)
                states[s + 1] = Math.min(states[s], states[s + 1]);
              break;
            case NONE:
              if (match) {
                if (s < length - 1)
                  next[s + 1] = Math.min(states[s], next[s + 1]);
                else
                  limits[lcount++] = (((long)states[s]) << 32) | (index + 1);
              }
              break;
            case STAR:
              if (s < length - 1)
                states[s + 1] = Math.min(states[s], states[s + 1]);
              else if (match || states[s] < index)
                limits[lcount++] = (((long)states[s]) << 32) | (index + ((match) ? 1 : 0));

              if (match)
                next[s] = states[s];
              break;
          }
        }
        states[s] = seqSize;
      }
      index++;

      for (int r = relIndex; r < lcount; r++) {
        final long c = limits[r];
        final long diff = c - last;
        if (diff >= 0x100000000L)
          limits[relIndex++] = last = c;
        else if (diff > 0)
          limits[relIndex - 1] = last = c;
      }
    }

    for (int r = 0; r < relIndex; r++) {
      final int start = (int)(limits[r] >> 32);
      final int end = (int)limits[r];
      visitor.found(start, end);
    }
  } catch (ArrayIndexOutOfBoundsException aioobe) {
    this.limits = new long[this.limits.length + 50];
    match(seq, visitor);
  }
  }

  @Override
  public Pattern<Character> pattern() {
    return pattern;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final SimpleRegExp that = (SimpleRegExp) o;

    return !(pattern != null ? !pattern.equals(that.pattern) : that.pattern != null);
  }

  @Override
  public int hashCode() {
    return pattern != null ? pattern.hashCode() : 0;
  }

  public boolean match(final CharSeq seq) {
    final int[] counter = new int[]{0};
    match(seq, new MatchVisitor() {
      @Override
      public boolean found(final int start, final int end) {
        counter[0]++;
        return false;
      }
    });
    return counter[0] == 1;
  }

  public int count(final CharSeq seq) {
    final int[] counter = new int[]{0};
    match(seq, new MatchVisitor() {
      @Override
      public boolean found(final int start, final int end) {
        counter[0]++;
        return false;
      }
    });
    return counter[0];
  }
}