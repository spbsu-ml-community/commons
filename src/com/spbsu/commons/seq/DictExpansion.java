package com.spbsu.commons.seq;

import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecTools;
import com.spbsu.commons.math.vectors.impl.ArrayVec;
import com.spbsu.commons.text.CharSequenceTools;
import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.procedure.TLongIntProcedure;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: solar
 * Date: 04.06.12
 * Time: 18:23
 */
public class DictExpansion {
  private final double alpha;
  private final int alphabetSize;
  private ListDictionary start;
  private ListDictionary current;
  private ListDictionary result; // updated on reduce
  private double minProb;

  private final int size;

  private int statPower = 0;
  private int pairsCount = 0;
  private TLongIntHashMap freqs = new TLongIntHashMap();

  public DictExpansion(Collection<Character> alphabet, int size, double alpha) {
    this(new ListDictionary(alphabet.toArray(new Character[alphabet.size()])), size, alpha);
  }

  public DictExpansion(ListDictionary alphabet, int size, double alpha) {
    this.start = alphabet;
    this.alphabetSize = alphabet.size();
    this.alpha = alpha;
    current = result = alphabet;
    freqs = new TLongIntHashMap((int)MathTools.sqr(alphabet.size()));
    minProb = 1. / MathTools.sqr(alphabet.size());
    this.size = size;
  }

  private final class StatItem {
    long code;
    int first;
    int second;
    double score;
    int count;

    private StatItem(long code, int first, int second, double score, int count) {
      this.code = code;
      this.first = first;
      this.second = second;
      this.score = score;
      this.count = count;
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder();
      if (first >= 0)
        result.append(current.get(first));
      result.append(current.get(second));
      result.append("->(");
      result.append(count);
      result.append(", ").append(score);
      result.append(")");
      return result.toString();
    }
  }
  public void accept(CharSequence suffix) {
    int prev = 0;
    while(suffix.length() > 0) {
      final int symbol = current.search(suffix) + 1;
      freqs.adjustOrPutValue(symbol, 1, 1);
      if (prev > 0) {
        freqs.adjustOrPutValue((((long)prev << 32) | symbol), 1, 1);
        pairsCount++;
      }
      prev = symbol;
      suffix = suffix.subSequence(current.get(symbol - 1).length(), suffix.length());
      statPower++;
    }

    if (statPower > -Math.log(alpha) / minProb) {
      if (current != result) {
        result = current = trivialReduce();
      }
      else current = expand();

      freqs.clear();
      for (int c = 0; c < current.size(); c++) {
        freqs.put(c + 1, 0);
      }
      statPower = 0;
      pairsCount = 0;
    }
  }

  private ListDictionary expand() {
    final List<StatItem> items = new ArrayList<StatItem>();
    final double denomSingle = statPower + current.size();

    freqs.forEachEntry(new TLongIntProcedure() {
      @Override
      public boolean execute(long code, int count) {
        final int second = (int) ((code & 0xFFFFFFFFl) - 1);
        final int first = (int) ((code >> 32) - 1);
        if (first >= 0) {
          final double pairProbIndependentDirichlet = (freqs.get(first + 1) + 1.) * (freqs.get(second + 1) + 1.) / denomSingle / denomSingle;
          final double lambda = pairsCount * pairProbIndependentDirichlet;
          final double logProb = MathTools.logPoissonProbability(lambda, count);
          items.add(new StatItem(code, first, second, count > lambda ? logProb : 0, count));
        }
        return true;
      }
    });

    List<CharSequence> newDict = new ArrayList<CharSequence>(size);
    Collections.sort(items, new Comparator<StatItem>() {
      @Override
      public int compare(StatItem o1, StatItem o2) {
        return Double.compare(o1.score, o2.score);
      }
    });

    int slots = Math.max(size / 10, alphabetSize);
    newDict.addAll(current.alphabet());
    minProb = 1;
    for (StatItem item : items) {
      if (--slots < 0 || item.score >= Math.log(0.01))
        break;
      newDict.add(CharSequenceTools.concat(current.get(item.first), current.get(item.second)));
      minProb = Math.min((item.count + 1) /((double)statPower + current.size()), minProb);
    }
    return new ListDictionary(newDict.toArray(new CharSequence[newDict.size()]));
  }

  private ListDictionary trivialReduce() {
    final List<StatItem> items = new ArrayList<StatItem>();

    freqs.forEachEntry(new TLongIntProcedure() {
      @Override
      public boolean execute(long code, int count) {
        final int second = (int) ((code & 0xFFFFFFFFl) - 1);
        if (code > 0xFFFFFFFFl) // composite
          return true;
        final int parent = current.parent(second);
        if (parent < 0) {
          items.add(new StatItem(code, -1, second, Double.MAX_VALUE, count));
          return true;
        }
        items.add(new StatItem(code, -1, second, count, count));
        return true;
      }
    });

    List<CharSequence> newDict = new ArrayList<CharSequence>(size);
    Collections.sort(items, new Comparator<StatItem>() {
      @Override
      public int compare(StatItem o1, StatItem o2) {
        return Double.compare(o2.score, o1.score);
      }
    });

    int slots = size - alphabetSize;
    final double limit = statPower * minProb;
    minProb = 1;
    for (StatItem item : items) {
      if (item.score < limit)
        break;
      minProb = Math.min((item.count + 1) /((double)statPower + current.size()), minProb);
      final CharSequence symbol = current.get(item.second);
      if (symbol.length() == 1 || slots-- > 0) { // alphabet element or empty slot
        newDict.add(symbol);
      }
    }
    minProb = Math.pow(minProb, 1.3);
    return new ListDictionary(newDict.toArray(new CharSequence[newDict.size()]));
  }

  private ListDictionary reduce() {
    final List<StatItem> items = new ArrayList<StatItem>();
    final double fEntropy;
    {
      Vec prob = new ArrayVec(current.size());
      for (int i = 0; i < current.size(); i++) {
        prob.set(i, (freqs.get(i + 1) + 1.) / (statPower + current.size()));
      }
      fEntropy = VecTools.entropy(prob);
    }

    freqs.forEachEntry(new TLongIntProcedure() {
      int[] counts = new int[current.size()];
      @Override
      public boolean execute(long code, int count) {
        final int second = (int) ((code & 0xFFFFFFFFl) - 1);
        final int first = (int) ((code >> 32) - 1);
        if (first >= 0)
          return true;
        final int parent = current.parent(second);
        if (parent < 0) {
          items.add(new StatItem(code, -1, second, Double.MAX_VALUE, count));
          return true;
        }
        Arrays.fill(counts, 0);
        counts[parent] += count;
        final CharSequence text = current.get(second);
        int statIncrement = count;
        for (int c = current.get(parent).length(); c < text.length();) {
          final int next = current.search(text.subSequence(c, text.length()));
          counts[next] += count;
          statIncrement += count;
          c += current.get(next).length();
        }
        Vec prob = new ArrayVec(current.size() - 1);
        { // entropy without symbol
          for (int i = 0, index = 0; i < current.size(); i++) {
            if (i == second)
              continue;
//              final double probab = (freqs.get(i + 1) + 1.) / (statPower + current.size() - 1 - count * current.get(second).length());
//            final double probab = (freqs.get(i + 1) + 1. + counts[i]) / (statPower + statIncrement + current.size() - 1);
//            final double probab = i != parent ? (freqs.get(i + 1) + 1.) / (statPower + current.size() - 1) : (freqs.get(i + 1) + count + 1.) / (statPower + current.size() - 1);
            final double probab = (freqs.get(i + 1) + 1.) / (statPower - count + current.size() - 1);
            prob.set(index++, probab);
          }
        }
        items.add(new StatItem(code, -1, second, count/*fEntropy - VecTools.entropy(prob)*/, count));
        return true;
      }
    });

    List<CharSequence> newDict = new ArrayList<CharSequence>(size);
    Collections.sort(items, new Comparator<StatItem>() {
      @Override
      public int compare(StatItem o1, StatItem o2) {
        return Double.compare(o2.score, o1.score);
      }
    });

    minProb = 0;
    int slots = size - alphabetSize;
    for (StatItem item : items) {
      if (item.score < 0.05)
        break;
      minProb += (item.count + 1) /((double)statPower + current.size());
      final CharSequence symbol = current.get(item.second);
      if (symbol.length() == 1 || slots-- > 0) { // alphabet element or empty slot
        newDict.add(symbol);
      }
    }
    minProb /= items.size();
    minProb *= minProb;
    return new ListDictionary(newDict.toArray(new CharSequence[newDict.size()]));
  }

  public ListDictionary result() {
    return result;
  }
}