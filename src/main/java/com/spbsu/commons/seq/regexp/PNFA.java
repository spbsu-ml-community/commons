package com.spbsu.commons.seq.regexp;

import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.seq.Sequence;

import java.util.Arrays;
import java.util.Random;

/**
 * User: solar
 * Date: 27.12.11
 * Time: 12:08
 */
public class PNFA<T> implements Matcher<T> {
  public static final double C = 1;
  private final long[] transitions;
  private final double[] pro_bab;
  private final int[] states;
  private final Alphabet<T> alphabet;
  private final Random rand = new FastRandom();

  public PNFA(Vec pro_bab, Alphabet<T> alpha) {
    alphabet = alpha;

    final int a = pro_bab.dim() / alpha.size();
    final int statesCount = (int) ((Math.sqrt(8 * a + 1) - 1) / 2.); //
    states = new int[statesCount * 2];
    transitions = new long[pro_bab.dim()];
    this.pro_bab = new double[pro_bab.dim()];
    for (int c = 0; c < alpha.size(); c++) {
      int sindex = 0;
      for (int from = 0; from < statesCount; from++) {
        for (int to = from; to < statesCount; to++) {
          int index = c * a + sindex++;
          transitions[index] = (((long) to << 32) | from);
          this.pro_bab[index] = C / (C + Math.exp(-pro_bab.get(index)));
        }
      }
    }
  }

//  public void setProbability(int from, int to, int condition, double prob) {
//    final int statesCount = states.length / 2;
//    final int coffset = condition * pro_bab.length / alphabet.size();
//    pro_bab[coffset + statesCount * from - from * (from + 1) / 2 + to] = prob;
//  }
//
  public void match(Sequence<T> ts, final MatchVisitor visitor) {
    final int seqSize = ts.length();
    final int[] statesLocal = states;
    final int statesCount = statesLocal.length / 2;
    final double[] probabLocal = pro_bab;

    int matchStart = 0, matchEnd = -1;
    for (int i = 0; i < statesCount * 2; i++) {
      statesLocal[i] = seqSize;
    }

    for (int t = 0; t < seqSize; t++) {
      final int input = alphabet.index(ts, t);
      final int nShift = ((t + 1) & 1) * statesCount;
      for (int i = 0; i < statesCount; i++) {
        statesLocal[nShift + i] = seqSize;
      }

      final int cShift = (t & 1) * statesCount;
      if (statesLocal[cShift] >= t)
        statesLocal[cShift] = t;
      int pShift = input * statesCount * (statesCount + 1)/2;
      for (int from = 0; from < statesCount - 1; from++) {
        final int fromStart = statesLocal[from + cShift];
        if (fromStart < seqSize) {
          for (int to = from; to < statesCount; to++) {
            if (statesLocal[to + nShift] > fromStart && rand.nextDouble() < probabLocal[pShift++])
              statesLocal[to + nShift] = fromStart;
          }
        }
        else pShift += statesCount - from;
      }
      final int finalStart = statesLocal[nShift + statesCount - 1];
      if (finalStart < seqSize) {
        if (matchStart < finalStart && matchStart <= matchEnd)
          visitor.found(matchStart, matchEnd + 1);
        matchStart = finalStart;
        matchEnd = t;
      }
    }
    if (matchStart <= matchEnd)
      visitor.found(matchStart, matchEnd + 1);
  }

  @Override
  public Pattern<T> pattern() {
    return null;
  }

  public double p(int index) {
    return pro_bab[index];
  }

  public int from(int index) {
    final long currentTrans = transitions[index];
    return (int) currentTrans;
  }
  
  public int to(int index) {
    final long currentTrans = transitions[index];
    return (int) (currentTrans >> 32);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PNFA pnfa = (PNFA) o;

    if (alphabet != null ? !alphabet.equals(pnfa.alphabet) : pnfa.alphabet != null) return false;
    if (!Arrays.equals(pro_bab, pnfa.pro_bab)) return false;
    if (rand != null ? !rand.equals(pnfa.rand) : pnfa.rand != null) return false;
    if (!Arrays.equals(states, pnfa.states)) return false;
    if (!Arrays.equals(transitions, pnfa.transitions)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = transitions != null ? Arrays.hashCode(transitions) : 0;
    result = 31 * result + (pro_bab != null ? Arrays.hashCode(pro_bab) : 0);
    result = 31 * result + (states != null ? Arrays.hashCode(states) : 0);
    result = 31 * result + (alphabet != null ? alphabet.hashCode() : 0);
    result = 31 * result + (rand != null ? rand.hashCode() : 0);
    return result;
  }

  //  public static class Path {
//    private TIntArrayList path;
//    private ProbabilisticNFA probabilisticNFA;
//
//    Path(ProbabilisticNFA probabilisticNFA, TIntArrayList path) {
//      this.probabilisticNFA = probabilisticNFA;
//      this.path = path;
//    }
//
//    public int from(int index) {
//      final int idx = path.at(index);
//      return (int) probabilisticNFA.transitions[idx];
//    }
//
//    public int to(int index) {
//      final int idx = path.at(index);
//      final long currentTrans = probabilisticNFA.transitions[idx];
//      return (int) (currentTrans >> 32);
//    }
//
//    public int condition(int index) {
//      final int idx = path.at(index);
//      final int statesCount = probabilisticNFA.states.length / 2;
//      return idx / (statesCount * (statesCount + 1) / 2);
//    }
//
//    public double probability(int index) {
//      int idx = path.at(index);
//      return probabilisticNFA.pro_bab[idx];
//    }
//
//    public int length() {
//      return path.size();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//      if (this == o) return true;
//      if (o == null || getClass() != o.getClass()) return false;
//      Path path1 = (Path) o;
//      return path.equals(path1.path);
//    }
//
//    @Override
//    public int hashCode() {
//      return path.hashCode();
//    }
//
//    public int index(int i) {
//      return path.at(i);
//    }
//  }
}
