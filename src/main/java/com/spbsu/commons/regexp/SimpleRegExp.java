package com.spbsu.commons.regexp;

import com.spbsu.commons.seq.Sequence;

import java.util.Arrays;

/**
 * User: solar
 * Date: 12.09.11
 * Time: 20:18
 */
public class SimpleRegExp<T> implements Matcher<T> {

  private final Pattern<T> processedExpression;
  private Pattern<T> pattern;

  public SimpleRegExp(final Pattern<T> expression) {
    pattern = expression;
    processedExpression = new Pattern<T>(expression.alphabet());
    int prevMod = 0;
    Condition<T> prevCon = null;
    int expressionSize = expression.size();
    for (int s = 0; s < expressionSize; s++) {
      final Pattern.Modifier mod = expression.modifier(s);
      final Condition<T> con = expression.condition(s);
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

  long[] limits = new long[50];
  final int[][] statesStore;

  public void match(final Sequence<T> sequence,
                    final MatchVisitor visitor) {
    final Pattern<T> expression = processedExpression;
    int index = 0;
    int seqSize = sequence.size();
    final int[][] statesStoreLocal = statesStore;
    Arrays.fill(statesStoreLocal[0], seqSize);
    Arrays.fill(statesStoreLocal[1], seqSize);

    long[] limits = this.limits;
    int relIndex = 0;
    long last = 0xFFFFFFFF00000000L;

    try {
    for (int i = 0; i < seqSize + 1; i++) {
      int lcount = relIndex;
      final T current = i < seqSize ? sequence.get(i) : sequence.get(i - 1);
      int[] states = statesStoreLocal[index % 2];
      int[] next = statesStoreLocal[(index + 1) % 2];
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
    match(sequence, visitor);
  }
  }

  @Override
  public Pattern<T> pattern() {
    return pattern;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SimpleRegExp that = (SimpleRegExp) o;

    if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return pattern != null ? pattern.hashCode() : 0;
  }
}