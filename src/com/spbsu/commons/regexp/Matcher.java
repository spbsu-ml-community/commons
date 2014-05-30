package com.spbsu.commons.regexp;

import com.spbsu.commons.seq.Sequence;

/**
 * Created by IntelliJ IDEA.
 * User: solar
 * Date: 23.12.11
 * Time: 15:06
 * To change this template use File | Settings | File Templates.
 */
public interface Matcher<T> {
  interface Condition<T> {
    boolean is(T frag);
    static final Condition ANY = new Condition() {
      public boolean is(Object frag) {
        return true;
      }
      @Override
      public String toString() {
        return ".";
      }
    };

  }
  interface MatchVisitor {
    boolean found(int start, int end);
  }

  void match(final Sequence<T> sequence, final MatchVisitor visitor);
  Pattern<T> pattern();
}
