package com.spbsu.commons.io.codec.seq;

import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.seq.Seq;
import com.spbsu.commons.util.ArrayTools;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.procedure.TLongIntProcedure;

import java.util.*;

import static java.lang.Math.log;
import static java.lang.Math.min;

/**
 * Created with IntelliJ IDEA.
 * User: solar
 * Date: 04.06.12
 * Time: 18:23
 */
public class DictExpansion<T extends Comparable<T>> {
  public static final double POISSON_SIGNIFICANCE = 0.01;
  public static final double EXTENSION_FACTOR = 1.33;
  public static final double MAX_POWER = 100000000;
  public static final double MAX_MIN_PROBABILITY = 0.01;
  private final int size;
  private final boolean trace;
  private ListDictionary<T> suggest;
  private ListDictionary<T> current;
  private ListDictionary<T> result;

  private int powerSuggest = 0;
  private int powerCurrent = 0;
  private double minProbCurrent = 1;
  private double minProbSuggest = 1;

  private int pairsCount = 0;
  private TLongIntHashMap pairFreqs = new TLongIntHashMap();
  private int[] symbolFreqsCurrent = null;
  private int[] symbolFreqsSuggest = null;
  private int[] resultFreqs = null;
  private int alphabetSize;
  private double probFound = 0.1;
  private double bestCompressionRate = 1;
  private int noRateIncreaseTurns = 0;

  public DictExpansion(Collection<T> alphabet, int size) {
    this(alphabet, size, false);
  }

  public DictExpansion(Collection<T> alphabet, int size, boolean trace) {
    this(new ListDictionary<>(ArrayTools.toArray(alphabet)), size, trace);
  }

  public DictExpansion(ListDictionary<T> alphabet, int size) {
    this(alphabet, size, false);
  }

  public DictExpansion(ListDictionary<T> alphabet, int size, boolean trace) {
    this.size = size;
    this.trace = trace;
    this.alphabetSize = alphabet.size();
    current = suggest = alphabet;
    symbolFreqsCurrent = new int[current.size()];
    symbolFreqsSuggest = new int[suggest.size()];
    pairFreqs.clear();
    powerSuggest = 0;
    powerCurrent = 0;
    pairsCount = 0;
    pairFreqs = new TLongIntHashMap((int) (size * EXTENSION_FACTOR * 2));
    minProbCurrent = MAX_MIN_PROBABILITY;
    minProbSuggest = MAX_MIN_PROBABILITY;
  }

  public ListDictionary<T> result() {
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
  public void accept(Seq<T> seq) {
    int prev = -1;
    {
      Seq<T> suffix = seq;
      while(suffix.length() > 0) {
        final int symbol = current.search(suffix);
        symbolFreqsCurrent[symbol]++;
        if (prev >= 0) {
          pairFreqs.adjustOrPutValue((long)prev << 32 | symbol, 1, 1);
          pairsCount++;
        }
        prev = symbol;
        suffix = suffix.sub(current.get(symbol).length(), suffix.length());
        powerCurrent++;
      }
    }
    {
      Seq<T> suffix = seq;
      while(suffix.length() > 0) {
        final int symbol = suggest.search(suffix);
        symbolFreqsSuggest[symbol]++;
        suffix = suffix.sub(suggest.get(symbol).length(), suffix.length());
        powerSuggest++;
      }
    }

    if ((powerSuggest > -log(probFound) / minProbSuggest && powerCurrent > -log(probFound) / minProbCurrent) || powerSuggest > MAX_POWER) {
      double sum = 0;
      double textLength = 0;
      for (int i = 0; i < current.size(); i++) {
        final int freq = symbolFreqsCurrent[i];
        textLength += current.get(i).length() * freq;
        if (freq > 0)
          sum -= freq * log(freq) / log(2);
      }
      double codeLength = (sum + powerCurrent * log(powerCurrent) / log(2)) / 8.;
      final double compressionRate = codeLength / textLength;
      if (compressionRate < bestCompressionRate) {
        bestCompressionRate = compressionRate;
        noRateIncreaseTurns = 0;
      }
      else if (++noRateIncreaseTurns > 3) {
        probFound *= 0.8;
      }

      result = current;
      resultFreqs = symbolFreqsCurrent;

      if (trace)
        System.out.println("Size: " + current.size() + " rate: " + compressionRate + " minimal probability: " + minProbSuggest);

      final ListDictionary<T> reduce = reduce();
      final ListDictionary<T> expand = expand();

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

  private ListDictionary<T> expand() {
    final List<StatItem> items = new ArrayList<>();
    pairFreqs.forEachEntry(new TLongIntProcedure() {
      @Override
      public boolean execute(long code, int count) {
        final int first = (int) (code >>> 32);
        final int second = (int) (code & 0xFFFFFFFFl);
        final double pairProbIndependentDirichlet = symbolFreqsCurrent[first] * symbolFreqsCurrent[second] / (double) powerCurrent / (double) powerCurrent;
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
    final List<Seq<T>> newDict = new ArrayList<>(current.alphabet());
    int slots = (int)(current.size() * (EXTENSION_FACTOR - 1)) + alphabetSize;
    minProbSuggest = minProbCurrent;
    for (StatItem item : items) {
      if (item.score >= Math.log(POISSON_SIGNIFICANCE) || --slots < 0)
        break;
      newDict.add(CharSeqTools.concat(current.get(item.first), current.get(item.second)));
      minProbSuggest = min(minProbSuggest, item.count / (double)pairsCount);
    }
    //noinspection unchecked
    return new ListDictionary<T>(newDict.toArray(new Seq[newDict.size()]));
  }

  private ListDictionary<T> reduce() {
    final List<StatItem> items = new ArrayList<>();
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
    final List<Seq<T>> newDict = new ArrayList<>(suggest.size());

    for (int s = 0; s < symbolFreqsSuggest.length; s++) {
      final int parent = suggest.parent(s);
      final int count = symbolFreqsSuggest[s];
      Seq<T> seq = suggest.get(s);
      if (parent < 0)
        newDict.add(seq);
      else if (count > 0) {
        double codeLengthWOSymbol = codeLength + count * log(count);
        int newStatPower = powerSuggest - count;
        int next = parent;
        do {
          seq = seq.sub(suggest.get(next).length(), seq.length());
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

    int slots = size - alphabetSize;
    minProbCurrent = min(1. / current.size(), MAX_MIN_PROBABILITY);
    for (StatItem item : items) {
      if (item.score < 0. || --slots < 0)
        break;
      final double p = (item.count + 1) / ((double) powerSuggest + suggest.size());
      if (slots > size / 10.)
        minProbSuggest = min(p, minProbSuggest);
      final Seq<T> symbol = suggest.get(item.second);
      newDict.add(symbol);
    }
    //noinspection unchecked
    return new ListDictionary<T>(newDict.toArray(new Seq[newDict.size()]));
  }

  public int[] resultFreqs() {
    return resultFreqs;
  }
}
