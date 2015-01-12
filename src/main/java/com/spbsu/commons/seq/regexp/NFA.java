package com.spbsu.commons.seq.regexp;

import com.spbsu.commons.seq.Seq;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: solar
 * Date: 23.12.11
 * Time: 15:03
 */
public class NFA<T> implements Matcher<T>{
  private final long[] transitions;
  private final long[] transitionsIndex;
  private final boolean[] finalStates;
  private final int[] states;
  private final Alphabet<T> alphabet;

  public NFA(final List<TLongHashSet> programs, final TIntHashSet finalStates, final Alphabet<T> alpha) {
    final int statesCount = programs.size() + 1;
    states = new int[statesCount * 2];
    this.finalStates = new boolean[statesCount];
    finalStates.forEach(new TIntProcedure() {
      @Override
      public boolean execute(final int i) {
        NFA.this.finalStates[i] = true;
        return true;
      }
    });
    alphabet = alpha;
    final Map<Integer, TLongHashSet> transitionsMap = new HashMap<Integer, TLongHashSet>();
    for (int i = 0; i < programs.size(); i++) {
      final int from = i;
      final TLongHashSet instructions = programs.get(from);
      instructions.forEach(new TLongProcedure() {
        @Override
        public boolean execute(final long l) {
          final int condition = (int)l;
          final int to = (int)(l >> 32);
          TLongHashSet trans = transitionsMap.get(condition);
          if (trans == null)
            transitionsMap.put(condition, trans = new TLongHashSet());
          trans.add(((long)from << 32) | (long)to);
          return true;
        }
      });
    }
    
    final TLongHashSet current = new TLongHashSet();
    final TLongArrayList transitions = new TLongArrayList();
    final TLongArrayList transitionsIndex = new TLongArrayList();
    if (transitionsMap.containsKey(alphabet.size())) {
      current.addAll(transitionsMap.get(alphabet.size()).toArray());
      final long[] trans = current.toArray();
      Arrays.sort(trans);
      for (int t = 0; t < trans.length; t++) {
        final long tran = trans[t];
        transitions.add((tran >> 32) | (tran << 32));
      }
    }
    final int defaultEnd = transitions.size();

    for (int i = 0; i < alphabet.size(); i++) {
      if (transitionsMap.containsKey(i)) {
        final long transitionsStart = ((long)transitions.size()) << 32;
        current.clear();
        current.addAll(transitionsMap.get(i).toArray());
        if (transitionsMap.containsKey(alphabet.size()))
          current.addAll(transitionsMap.get(alphabet.size()).toArray());
        final long[] trans = current.toArray();
        Arrays.sort(trans);
        for (int t = 0; t < trans.length; t++) {
          final long tran = trans[t];
          transitions.add((tran >> 32) | (tran << 32));
        }
        transitionsIndex.add(transitionsStart | transitions.size());
      }
      else transitionsIndex.add(defaultEnd);
    }
    this.transitions = transitions.toArray();
    this.transitionsIndex = transitionsIndex.toArray();
  }

  @Override
  public void match(final Seq<T> ts, final MatchVisitor visitor) {
    final int seqSize = ts.length();
    final int[] statesLocal = states;
    final long[] transitionsIndexLocal = transitionsIndex;
    final long[] transitionsLocal = transitions;
    final boolean[] finalStatesLocal = finalStates;

    final int statesCount = statesLocal.length / 2;
    int matchStart = 0, matchEnd = -1;
    Arrays.fill(statesLocal, seqSize);
    
    final boolean[] hasElements = new boolean[]{true, true};

    for (int t = 0; t < seqSize; t++) {
      final int input = alphabet.index(ts.at(t));
      final long ti = transitionsIndexLocal[input];
      final int startTI = (int)(ti >> 32);
      final int endTI = (int) ti;
      final int nindex = (t+1)&1;
      final int nShift = nindex * statesCount;
      if (hasElements[nindex]) {
        for (int i = 0; i < statesCount; i++)
          statesLocal[nShift + i] = seqSize;
        hasElements[nindex] = false;
      }
      if (startTI == endTI)
        continue;

      final int cShift = (t&1) * statesCount;
      statesLocal[cShift] = Math.min(statesLocal[cShift], t);
      int minStart = seqSize;
      for (int currentTI = startTI; currentTI < endTI; currentTI++) {
        final long currentTrans = transitionsLocal[currentTI];
        final int from = (int)currentTrans;
        final int cIndex = cShift + from;
        final int pos4State = statesLocal[cIndex];
        if (pos4State < seqSize) {
          hasElements[nindex] = true;
          final int to = (int) (currentTrans >> 32);
          final int nIndex = nShift + to;
          final int start = statesLocal[nIndex] = Math.min(statesLocal[nIndex], pos4State);
          if (finalStatesLocal[to])
            minStart = Math.min(start, minStart);
        }
      }
      statesLocal[cShift] = seqSize;
      if (minStart < seqSize) {
        if (matchStart < minStart && matchStart <= matchEnd)
          visitor.found(matchStart, matchEnd + 1);
        matchStart = minStart;
        matchEnd = t;
      }
    }
    if (matchStart <= matchEnd)
      visitor.found(matchStart, matchEnd + 1);
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final NFA nfa = (NFA) o;

    if (alphabet != null ? !alphabet.equals(nfa.alphabet) : nfa.alphabet != null) return false;
    if (!Arrays.equals(finalStates, nfa.finalStates)) return false;
    if (states.length != states.length) return false;
    if (!Arrays.equals(transitions, nfa.transitions)) return false;
    if (!Arrays.equals(transitionsIndex, nfa.transitionsIndex)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = transitions != null ? Arrays.hashCode(transitions) : 0;
    result = 31 * result + (transitionsIndex != null ? Arrays.hashCode(transitionsIndex) : 0);
    result = 31 * result + Arrays.hashCode(finalStates);
    result = 31 * result + states.length;
    result = 31 * result + alphabet.hashCode();
    return result;
  }

  @Override
  public Pattern<T> pattern() {
    return null;
  }

  public int states() {
    return states.length / 2;
  }

  public boolean connected(final int stateA, final int stateB, final int conditionIndex) {
    int start = (int)(transitionsIndex[conditionIndex] >> 32);
    final int end = (int)(transitionsIndex[conditionIndex]);
    final long cmp = ((long)stateB << 32) | stateA;
    while(start < end) {
      if(transitions[start++] == cmp)
        return true;
    }
    return false;
  }

  @Override
  public String toString() {
    final int statesCount = this.states.length;
    final StringBuilder builder = new StringBuilder();
    for (int stateA = 0; stateA < statesCount - 1; stateA++) {
      boolean first = true;
      for (int stateB = stateA; stateB < statesCount; stateB++) {
        for (int i = 0; i < alphabet.size(); i++) {
          if (connected(stateA, stateB, i)) {
            if (!first)
              builder.append(", ");
            else
              builder.append("\t").append(stateA).append(": ");
            builder.append(String.format("%s->%d", alphabet.getT(alphabet.get(i)), stateB));
            first = false;
          }
        }
      }
      if (!first)
        builder.append("\n");
    }
    return builder.toString();
  }
}
