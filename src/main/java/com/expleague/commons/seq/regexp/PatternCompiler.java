package com.expleague.commons.seq.regexp;

import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;
import java.util.List;

/**
 * User: solar
 * Date: 24.12.11
 */
public class PatternCompiler {
  final List<TLongHashSet> programs = new ArrayList<TLongHashSet>();

  private void connect(final int from, final int to, final int conditionIndex) {
    while (programs.size() <= from)
      programs.add(new TLongHashSet());
    programs.get(from).add((((long)to) << 32) | conditionIndex);
  }
  
  public <T> NFA<T> compile(final Pattern<T> pattern) {
    final Alphabet<T> alphabet = pattern.alphabet();
    final TIntHashSet active = new TIntHashSet();
    active.add(0);
    for (int i = 0; i < pattern.size(); i++) {
      final int state = i + 1;
      final int conditionIndex = alphabet.indexCondition(pattern.condition(state - 1));
      active.forEach(new TIntProcedure() {
        @Override
        public boolean execute(final int a) {
          connect(a, state, conditionIndex);
          return true;
        }
      });
      switch (pattern.modifier(i)) {
        case NONE:
          active.clear();
          break;
        case STAR:
          connect(state, state, conditionIndex);
          break;
      }
      active.add(state);
    }
    return new NFA<T>(programs, active, pattern.alphabet());
  }
}
