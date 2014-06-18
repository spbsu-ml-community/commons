package com.spbsu.commons.seq;

import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.text.CharSequenceTools;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.procedure.TLongIntProcedure;

import java.util.*;

import static java.lang.Math.*;

/**
 * Created with IntelliJ IDEA.
 * User: solar
 * Date: 04.06.12
 * Time: 18:23
 */
public class DictExpansion {
  public static final double POISSON_SIGNIFICANCE = 0.01;
  public static final double EXTENSION_FACTOR = 1.3;
  public static final double MAX_POWER = 10000000;
  private final int size;
  private ListDictionary suggest;
  private ListDictionary current;
  private ListDictionary result;

  private int powerSuggest = 0;
  private int powerCurrent = 0;
  private double minProbResult = 1;
  private double minProbSuggest = 1;

  private int pairsCount = 0;
  private TLongIntHashMap pairFreqs = new TLongIntHashMap();
  private int[] symbolFreqsCurrent = null;
  private int[] symbolFreqsSuggest = null;
  private int[] resultFreqs = null;

  public DictExpansion(Collection<Character> alphabet, int size) {
    this(new ListDictionary(alphabet.toArray(new Character[alphabet.size()])), size);
  }

  public DictExpansion(ListDictionary alphabet, int size) {
    this.size = size;
    current = suggest = alphabet;
    symbolFreqsCurrent = new int[current.size()];
    symbolFreqsSuggest = new int[suggest.size()];
    pairFreqs.clear();
    powerSuggest = 0;
    powerCurrent = 0;
    pairsCount = 0;
    pairFreqs = new TLongIntHashMap((int) (size * EXTENSION_FACTOR * 2));
    minProbResult = 1./alphabet.size();
  }

  public ListDictionary result() {
    return result;
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
        result.append(suggest.get(first));
      result.append(suggest.get(second));
      result.append("->(");
      result.append(count);
      result.append(", ").append(score);
      result.append(")");
      return result.toString();
    }
  }
  public void accept(CharSequence seq) {
    int prev = -1;
    {
      CharSequence suffix = seq;
      while(suffix.length() > 0) {
        final int symbol = current.search(suffix);
        symbolFreqsCurrent[symbol]++;
        if (prev >= 0) {
          pairFreqs.adjustOrPutValue((long)prev << 32 | symbol, 1, 1);
          pairsCount++;
        }
        prev = symbol;
        suffix = suffix.subSequence(current.get(symbol).length(), suffix.length());
        powerCurrent++;
      }
    }
    {
      CharSequence suffix = seq;
      while(suffix.length() > 0) {
        final int symbol = suggest.search(suffix);
        symbolFreqsSuggest[symbol]++;
        suffix = suffix.subSequence(suggest.get(symbol).length(), suffix.length());
        powerSuggest++;
      }
    }

    if ((powerSuggest > -log(0.01) / minProbSuggest && powerCurrent > -log(0.01) / minProbResult) || powerSuggest > MAX_POWER) {
      {
        double sum = 0;
        double textLength = 0;
        for (int i = 0; i < current.size(); i++) {
          final int freq = symbolFreqsCurrent[i];
          textLength += current.get(i).length() * freq;
          if (freq > 0)
            sum -= freq * log(freq) / log(2);
        }
        double codeLength = (sum + powerCurrent * log(powerCurrent) / log(2)) / 8.;
        System.out.println("Size: " + current.size() + " rate: " + codeLength / textLength + " minimal probability: " + minProbSuggest);
      }

      final ListDictionary reduce = reduce();
      final ListDictionary expand = expand();

      result = current;
      resultFreqs = symbolFreqsCurrent;
      current = reduce;
      suggest = expand;

      symbolFreqsCurrent = new int[current.size()];
      symbolFreqsSuggest = new int[suggest.size()];
      pairFreqs.clear();
      powerSuggest = 0;
      powerCurrent = 0;
      pairsCount = 0;
    }
  }

  private ListDictionary expand() {
    final List<StatItem> items = new ArrayList<StatItem>();
    pairFreqs.forEachEntry(new TLongIntProcedure() {
      @Override
      public boolean execute(long code, int count) {
        final int first = (int) (code >>> 32);
        final int second = (int) (code & 0xFFFFFFFFl);
        final double pairProbIndependentDirichlet = symbolFreqsCurrent[first] * symbolFreqsCurrent[second] / (double) powerSuggest / (double) powerSuggest;
        final double lambda = pairsCount * pairProbIndependentDirichlet;
        final double logProb = MathTools.logPoissonProbability(lambda, count);
        items.add(new StatItem(code, first, second, count > lambda ? logProb : 0, count));
        return true;
      }
    });

    Collections.sort(items, new Comparator<StatItem>() {
      @Override
      public int compare(StatItem o1, StatItem o2) {
        return Double.compare(o1.score, o2.score);
      }
    });
    final List<CharSequence> newDict = new ArrayList<CharSequence>(current.alphabet());
    int slots = (int)(current.size() * (EXTENSION_FACTOR - 1)) + 1;
    minProbSuggest = minProbResult;
    for (StatItem item : items) {
      if (item.score >= Math.log(POISSON_SIGNIFICANCE) || --slots < 0)
        break;
      newDict.add(CharSequenceTools.concat(current.get(item.first), current.get(item.second)));
      minProbSuggest = min(minProbSuggest, item.count / (double)pairsCount);
    }
    return new ListDictionary(newDict.toArray(new CharSequence[newDict.size()]));
  }

  private ListDictionary reduce() {
    final List<StatItem> items = new ArrayList<StatItem>();
    final double codeLength;
    {
      double sum = 0;
      for (int i = 0; i < suggest.size(); i++) {
        final int freq = symbolFreqsSuggest[i];
        if (freq > 0)
          sum -= freq * log(freq);
      }
      codeLength = sum + powerSuggest * log(powerSuggest);
    }
    final List<CharSequence> newDict = new ArrayList<CharSequence>(suggest.size());
    final TIntArrayList resultFreqs = new TIntArrayList(suggest.size());
    final TDoubleArrayList resultScores = new TDoubleArrayList(suggest.size());

    for (int s = 0; s < symbolFreqsSuggest.length; s++) {
      final int parent = suggest.parent(s);
      final int count = symbolFreqsSuggest[s];
      CharSequence seq = suggest.get(s);
      if (parent < 0) {
        newDict.add(seq);
        resultFreqs.add(count + 1);
        resultScores.add(Double.POSITIVE_INFINITY);
      }
      else if (count > 0) {
        double codeLengthWOSymbol = codeLength + count * log(count);
        int newStatPower = powerSuggest - count;
        int next = parent;
        do {
          seq = seq.subSequence(suggest.get(next).length(), seq.length());
          final int oldFreq = symbolFreqsSuggest[next];
          final int newFreq = oldFreq + count;
          newStatPower += count;
          codeLengthWOSymbol -= newFreq * log(newFreq) - (oldFreq > 0 ? oldFreq * log(oldFreq) : 0);
        }
        while (seq.length() > 0 && (next = suggest.search(seq)) >= 0);
        codeLengthWOSymbol += newStatPower * log(newStatPower + suggest.size() - 1) - powerSuggest * log(powerSuggest + suggest.size());
        items.add(new StatItem(s, -1, s, codeLengthWOSymbol - codeLength, count));
      }
    }
    Collections.sort(items, new Comparator<StatItem>() {
      @Override
      public int compare(StatItem o1, StatItem o2) {
        return Double.compare(o2.score, o1.score);
      }
    });

    int slots = size;
    minProbResult = min(1. / current.size(), 0.01);
    for (StatItem item : items) {
      if (item.score < 0. || --slots < 0)
        break;
      final double p = (item.count + 1) / ((double) powerSuggest + suggest.size());
      if (slots > size / 10.)
        minProbSuggest = min(p, minProbSuggest);
      final CharSequence symbol = suggest.get(item.second);
      resultFreqs.add(item.count + 1);
      resultScores.add(item.score);
      newDict.add(symbol);
    }
    return new ListDictionary(newDict.toArray(new CharSequence[newDict.size()]));
  }

  public int[] resultFreqs() {
    return resultFreqs;
  }
}
