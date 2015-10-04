package com.spbsu.commons.io.codec.seq;

import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.seq.Seq;
import com.spbsu.commons.util.ArrayTools;
import com.spbsu.commons.util.JSONTools;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.procedure.TLongIntProcedure;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

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
  public static final double MAX_MIN_PROBABILITY = 0.0001;
  private final boolean isDynamic;
  private int size;
  private final PrintStream trace;
  private final Dictionary<T> initial;
  private volatile DictionaryWithStat<T> suggest;
  private volatile DictionaryWithStat<T> current;
  private DictionaryWithStat<T> result;

  private final int alphabetSize;
  private double probFound = 0.1;
  private double bestCompressionRate = 1;
  private int noRateIncreaseTurns = 0;

  public DictExpansion(final Collection<T> alphabet, final int size) {
    this(alphabet, size, null);
  }

  @Deprecated
  public DictExpansion(final Collection<T> alphabet, final int size, final boolean trace) {
    this(new ListDictionary<>(ArrayTools.toArray(alphabet)), size, trace ? System.out : null);
  }

  public DictExpansion(final Collection<T> alphabet, final int size, final PrintStream trace) {
    this(new ListDictionary<>(ArrayTools.toArray(alphabet)), size, trace);
  }

  public DictExpansion(final Dictionary<T> alphabet, final int size) {
    this(alphabet, size, null);
  }

  public DictExpansion(final Dictionary<T> alphabet, final int size, final PrintStream trace) {
    this.size = size;
    this.trace = trace;
    this.alphabetSize = alphabet.size();
    initial = alphabet;
    isDynamic = !(alphabet instanceof ListDictionary);
    //noinspection unchecked
    current = createDict((Collection<Seq<T>>)alphabet.alphabet(), isDynamic, MAX_MIN_PROBABILITY);
    //noinspection unchecked
    suggest = createDict((Collection<Seq<T>>)alphabet.alphabet(), isDynamic, MAX_MIN_PROBABILITY);
  }

  public DictExpansion(int slots) {
    //noinspection unchecked
    this(Dictionary.EMPTY, slots, null);
  }

  public DictExpansion(int size, PrintStream trace) {
    this(Dictionary.EMPTY, size, trace);
  }

  @NotNull
  private static <T extends Comparable<T>> DictionaryWithStat<T> createDict(Collection<Seq<T>> alphabet, boolean isDynamic, double minProbResult) {
    //noinspection unchecked
    return new DictionaryWithStat<>(isDynamic ? new DynamicDictionary<>(alphabet) : new ListDictionary<>(alphabet.toArray(new Seq[alphabet.size()])), minProbResult);
  }

  public Dictionary<T> result() {
    return result.dict;
  }

  public Dictionary<T> alpha() {
    return initial;
  }

  final ThreadLocal<TIntIntMap> symbolFreqsCurrent = ThreadLocal.withInitial(new Supplier<TIntIntMap>() {
    @Override
    public TIntIntMap get() {
      return new TIntIntHashMap();
    }
  });
  final ThreadLocal<TIntIntMap> symbolFreqsSuggest = ThreadLocal.withInitial(new Supplier<TIntIntMap>() {
    @Override
    public TIntIntMap get() {
      return new TIntIntHashMap();
    }
  });

  final ThreadLocal<TLongIntMap> pairsFreqsCurrent = ThreadLocal.withInitial(new Supplier<TLongIntMap>() {
    @Override
    public TLongIntMap get() {
      return new TLongIntHashMap();
    }
  });

  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  public void accept(final Seq<T> seq) {
    final TIntIntMap symbolFreqsCurrent;
    final TIntIntMap symbolFreqsSuggest;
    final TLongIntMap pairsFreqsCurrent;
    lock.readLock().lock();
    final DictionaryWithStat<T> current = this.current;
    try {
      int prev = -1;
      symbolFreqsCurrent = this.symbolFreqsCurrent.get();
      symbolFreqsSuggest = this.symbolFreqsSuggest.get();
      pairsFreqsCurrent = this.pairsFreqsCurrent.get();
      symbolFreqsCurrent.clear();
      symbolFreqsSuggest.clear();
      pairsFreqsCurrent.clear();
      Seq<T> suffix = seq;
      { // parsing with current
        while (suffix.length() > 0) {
          final int symbol = current.search(suffix);
          symbolFreqsCurrent.adjustOrPutValue(symbol, 1, 1);
          if (prev >= 0) {
            pairsFreqsCurrent.adjustOrPutValue((long) prev << 32 | symbol, 1, 1);
          }
          prev = symbol;
          suffix = suffix.sub(current.get(symbol).length(), suffix.length());
        }
      }

      { // parsing with suggest
        suffix = seq;
        while (suffix.length() > 0) {
          final int symbol = suggest.search(suffix);
          symbolFreqsSuggest.adjustOrPutValue(symbol, 1, 1);
          suffix = suffix.sub(suggest.get(symbol).length(), suffix.length());
        }
      }
    }
    finally {
      lock.readLock().unlock();
    }

    lock.writeLock().lock();
    try {
      if (current != this.current)
        return;
      { // current update
        symbolFreqsCurrent.forEachEntry(new TIntIntProcedure() {
          @Override
          public boolean execute(final int symbol, final int freq) {
            DictExpansion.this.current.updateSymbol(symbol, freq);
            return true;
          }
        });
        pairsFreqsCurrent.forEachEntry(new TLongIntProcedure() {
          @Override
          public boolean execute(long pair, int count) {
            DictExpansion.this.current.updatePair(pair, count);
            return true;
          }
        });
      }

      { // suggest update
        symbolFreqsSuggest.forEachEntry(new TIntIntProcedure() {
          @Override
          public boolean execute(final int symbol, final int freq) {
            DictExpansion.this.suggest.updateSymbol(symbol, freq);
            return true;
          }
        });
      }
      update();
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  private boolean update() {
    if ((!current.enough(probFound) || !current.enough(probFound)) && suggest.power < MAX_POWER)
      return false;
    double sum = 0;
    double textLength = 0;
    for (int i = 0; i < current.size(); i++) {
      final int freq = current.freq(i);
      textLength += current.get(i).length() * freq;
      if (freq > 0)
        sum -= freq * log(freq) / log(2);
    }
    final double codeLength = (sum + current.power * log(current.power) / log(2)) / 8.;
    final double compressionRate = codeLength / textLength;
    if (compressionRate < bestCompressionRate) {
      bestCompressionRate = compressionRate;
      noRateIncreaseTurns = 0;
    } else if (++noRateIncreaseTurns > 3) {
      probFound *= 0.8;
    }

    result = current;

    if (trace != null) {
      final String message = "Size: " + current.size() + " rate: " + compressionRate + " minimal probability: " + suggest.minProbability;
      trace.println(message);
    }
    final DictionaryWithStat<T> currentLocal = current;
    current = suggest.reduce(size - alphabetSize, isDynamic);
    suggest = currentLocal.expand(min(size - alphabetSize, (int) (currentLocal.size() * (EXTENSION_FACTOR - 1))), isDynamic);

    return true;
  }

  public int[] resultFreqs() {
    return result.symbolFreqs.toArray();
  }

  public void print(Writer ps) throws IOException {
    final List<? extends Seq<T>> alphabet = result.alphabet();
    final double[] weights = new double[result.size()];
    ps.append("{\n");
    for (int i = 0; i < alphabet.size(); i++) {
      final Seq<T> tSeq = alphabet.get(i);
      for(int j = 0; j < weights.length; j++) {
        weights[j] = result.pairFreq((long) i << 32 | j);
      }
      final int[] order = ArrayTools.sequence(0, weights.length);
      ArrayTools.parallelSort(weights, order);

      final String symbol = tSeq.toString();
      ps.append(JSONTools.escape(symbol)).append(": {");
      for(int j = order.length - 1; j >= 0 && weights[j] > 0.001; j--) {
        if (j != order.length - 1)
          ps.append(",");
        final String expansion = alphabet.get(order[j]).toString();
        ps.append("\n").append(JSONTools.escape(expansion)).append(": ").append(CharSeqTools.ppDouble(weights[j] / (double) result.freq(i)));
      }
      ps.append("\n},\n");
    }
    ps.append("}\n");
  }

  private static class DictionaryWithStat<T extends Comparable<T>> implements Dictionary<T> {
    private final Dictionary<T> dict;
    private final TIntArrayList symbolFreqs;
    private int power = 0;
    private final TLongIntMap pairsFreqs = new TLongIntHashMap();
    private int pairsPower = 0;
    private final double minProbability;

    public DictionaryWithStat(Dictionary<T> dict, double minProbResult) {
      this.dict = dict;
      symbolFreqs = new TIntArrayList(dict.size());
      minProbability = minProbResult;
    }

    public void updateSymbol(int index, int freq) {
      if (index >= symbolFreqs.size())
        symbolFreqs.fill(symbolFreqs.size(), index + 1, 0);
      final int val = symbolFreqs.getQuick(index);
      symbolFreqs.setQuick(index, val + freq);
      power += freq;
    }

    public void updatePair(long pairIdx, int freq) {
      pairsFreqs.adjustOrPutValue(pairIdx, freq, freq);
      pairsPower += freq;
    }

    @Override
    public int search(Seq<T> seq) {
      return dict.search(seq);
    }

    @Override
    public Seq<T> get(int index) {
      return dict.get(index);
    }

    @Override
    public int size() {
      return dict.size();
    }

    @Override
    public List<? extends Seq<T>> alphabet() {
      return dict.alphabet();
    }

    @Override
    public int parent(int second) {
      return dict.parent(second);
    }

    public int freq(int index) {
      return index < symbolFreqs.size() ? symbolFreqs.getQuick(index) : 0;
    }

    public int pairFreq(long pairCode) {
      return pairsFreqs.get(pairCode);
    }

    public double codeLength() {
      double sum = 0;
      for (int i = 0; i < size(); i++) {
        final int freq = freq(i);
        if (freq > 0)
          sum -= freq * log(freq);
      }
      return sum + power * log(power);
    }

    private DictionaryWithStat<T> reduce(int slots, boolean isDynamic) {
      final List<StatItem> items = new ArrayList<>();
      final double codeLength = codeLength();
      final List<Seq<T>> newDict = new ArrayList<>(size());

      for (int s = 0; s < size(); s++) {
        final int parent = parent(s);
        final int count = freq(s);
        Seq<T> seq = get(s);
        if (parent < 0)
          newDict.add(seq);
        else if (count > 0) {
          double codeLengthWOSymbol = codeLength + count * log(count);
          int newStatPower = power - count;
          int next = parent;
          do {
            seq = seq.sub(get(next).length(), seq.length());
            final int oldFreq = freq(next);
            final int newFreq = oldFreq + count;
            newStatPower += count;
            codeLengthWOSymbol -= newFreq * log(newFreq) - (oldFreq > 0 ? oldFreq * log(oldFreq) : 0);
          }
          while (seq.length() > 0 && (next = search(seq)) >= 0);
          codeLengthWOSymbol += newStatPower * log(newStatPower + size() - 1) - power * log(power + size());
          items.add(new StatItem(s, -1, s, codeLengthWOSymbol - codeLength, count));
        }
      }
      Collections.sort(items, new Comparator<StatItem>() {
        @Override
        public int compare(final StatItem o1, final StatItem o2) {
          return Double.compare(o2.score, o1.score);
        }
      });

      double minProbResult = min(1. / size(), MAX_MIN_PROBABILITY);
      for (final StatItem item : items) {
        if (item.score < 0. || --slots < 0)
          break;
        final double p = (item.count + 1) / ((double) power + size());
        minProbResult = min(p, minProbResult);
        final Seq<T> symbol = get(item.second);
        newDict.add(symbol);
      }
      //noinspection unchecked
      return createDict(newDict, isDynamic, minProbResult);
    }

    private DictionaryWithStat<T> expand(int slots, boolean isDynamic) {
      final List<StatItem> items = new ArrayList<>();
      pairsFreqs.forEachEntry(new TLongIntProcedure() {
        @Override
        public boolean execute(final long code, final int count) {
          final int first = (int) (code >>> 32);
          final int second = (int) (code & 0xFFFFFFFFl);
          final double pairProbIndependentDirichlet = freq(first) * freq(second) / (double) power / (double) power;
          final double lambda = pairsPower * pairProbIndependentDirichlet;
          final double logProb = MathTools.logPoissonProbability(lambda, count);
          items.add(new StatItem(code, first, second, count > lambda ? logProb : 0, count));
          return true;
        }
      });

      Collections.sort(items, new Comparator<StatItem>() {
        @Override
        public int compare(final StatItem o1, final StatItem o2) {
          return Double.compare(o1.score, o2.score);
        }
      });
      final List<Seq<T>> newDict = new ArrayList<>(alphabet());
      double minProbResult = minProbability;
      for (final StatItem item : items) {
        if (item.score >= Math.log(POISSON_SIGNIFICANCE) || --slots < 0)
          break;
        newDict.add(CharSeqTools.concat(get(item.first), get(item.second)));
        minProbResult = min(minProbResult, item.count / (double)pairsPower);
      }
      //noinspection unchecked
      return createDict(newDict, isDynamic, minProbResult);
    }

    public boolean enough(double probFound) {
      return power > -log(probFound) / minProbability;
    }

    private final class StatItem {
      long code;
      int first;
      int second;
      double score;
      int count;

      private StatItem(final long code, final int first, final int second, final double score, final int count) {
        this.code = code;
        this.first = first;
        this.second = second;
        this.score = score;
        this.count = count;
      }

      @Override
      public String toString() {
        final StringBuilder result = new StringBuilder();
        if (first >= 0)
          result.append(get(first));
        result.append(get(second));
        result.append("->(");
        result.append(count);
        result.append(", ").append(score);
        result.append(")");
        return result.toString();
      }
    }
  }
}
