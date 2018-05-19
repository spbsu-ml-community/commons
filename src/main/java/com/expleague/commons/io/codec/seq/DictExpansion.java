package com.expleague.commons.io.codec.seq;

import com.expleague.commons.func.impl.WeakListenerHolderImpl;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.seq.*;
import com.expleague.commons.util.ArrayTools;
import com.expleague.commons.util.JSONTools;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.procedure.TIntDoubleProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

import static java.lang.Math.log;
import static java.lang.Math.min;

/**
 * Created with IntelliJ IDEA.
 * User: solar
 * Date: 04.06.12
 * Time: 18:23
 */
public class DictExpansion<T extends Comparable<T>> extends WeakListenerHolderImpl<DictExpansion<T>> {
  public static final double EXTENSION_FACTOR = 1.3;
  public static final double MAX_POWER = 20000000;
  public static final double MAX_MIN_PROBABILITY = 0.002;
  public static final int AGG_POWER = 100000;
  private final boolean isDynamic;
  private int size;
  private final PrintStream trace;
  private final Dictionary<T> initial;
  private volatile DictionaryWithStat<T> current;
  private boolean populate = true;
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
    current = createDict((List<Seq<T>>)alphabet.alphabet(), null, isDynamic, MAX_MIN_PROBABILITY);
    //noinspection unchecked
  }

  public DictExpansion(int slots) {
    //noinspection unchecked
    this(Dictionary.EMPTY, slots, null);
  }

  public DictExpansion(int size, PrintStream trace) {
    //noinspection unchecked
    this(Dictionary.EMPTY, size, trace);
  }

  @NotNull
  private static <T extends Comparable<T>> DictionaryWithStat<T> createDict(List<Seq<T>> alphabet, TIntArrayList initFreqs, boolean isDynamic, double minProbResult) {
//    if (alphabet.contains(CharSeq.create("игры для девочек")))
//      System.out.println("yes");
//    else
//      System.out.println("no");
    if (initFreqs != null && initFreqs.size() != alphabet.size())
      throw new IllegalArgumentException();
    //noinspection unchecked
    return new DictionaryWithStat<>(isDynamic ? new DynamicDictionary<>(alphabet) : new ListDictionary<>(alphabet.toArray(new Seq[alphabet.size()])), initFreqs, minProbResult);
  }

  public Dictionary<T> result() {
    return result != null ? result.dict : null;
  }

  public Dictionary<T> alpha() {
    return initial;
  }

  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  public void accept(final Seq<T> seq) {
    boolean enough;
    lock.readLock().lock();
    try {
      current.parse(seq);
      enough = (current.enough(probFound) || current.power > MAX_POWER);
    }
    finally {
      lock.readLock().unlock();
    }

    if (enough)
      update();
  }

  private void update() {
    lock.writeLock().lock();
    try {
      if (!((current.enough(probFound) || current.power > MAX_POWER)))
        return;
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


      DictionaryWithStat<T> result;
      if (populate) {
        result = current;
        invoke(this);
        if (trace != null)
          trace.println("Size: " + current.size() + " rate: " + compressionRate + " minimal probability: " + current.minProbability);
        int slots;
        if (current.size() * EXTENSION_FACTOR < 10)
          slots = size - alphabetSize;
        else
          slots = (int)(current.size() * EXTENSION_FACTOR);
        result = current.expand(slots, isDynamic);
      }
      else {
        this.result = result = current.reduce(size - alphabetSize, isDynamic);
      }
      current = result;
      populate = !populate;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public int[] resultFreqs() {
    lock.writeLock().lock();
    try {
      if (result.size() > result.symbolFreqs.size())
        result.symbolFreqs.fill(result.symbolFreqs.size(), result.size(), 0);
      return result.symbolFreqs.toArray();
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public void printPairs(Writer ps) throws IOException {
    lock.readLock().lock();
    final List<? extends Seq<T>> alphabet = result.alphabet();
    final TIntArrayList indices = new TIntArrayList();
    final TDoubleArrayList weights = new TDoubleArrayList();
    final int[] indicesArr = new int[result().size()];
    final double[] weightsArr = new double[result().size()];

    ps.append("{\n");
    try {
      for (int i = 0; i < alphabet.size(); i++) {
        final Seq<T> tSeq = alphabet.get(i);
        indices.clear();
        weights.clear();
        result.visitAssociations(i, (j, val) -> {
          indices.add(j);
          weights.add(val);
          return false;
        });
        weights.toArray(weightsArr, 0, weights.size());
        indices.toArray(indicesArr, 0, indices.size());
        ArrayTools.parallelSort(weightsArr, indicesArr, 0, indices.size());

        final String symbol = tSeq.toString();
        ps.append(JSONTools.escape(symbol)).append(": {");
        for (int j = indices.size() - 1; j >= 0 && weightsArr[j] > 0.001; j--) {
          if (j != indices.size() - 1)
            ps.append(",");
          final String expansion = alphabet.get(indicesArr[j]).toString();
          ps.append("\n").append(JSONTools.escape(expansion)).append(": ").append(CharSeqTools.ppDouble(weightsArr[j] / (double) result.freq(i)));
        }
        ps.append("\n},\n");
      }
    }
    finally {
      lock.readLock().unlock();
    }
    ps.append("}\n");
  }

  public void print(FileWriter fileWriter) throws IOException {
    lock.readLock().lock();

    try {
      for (int i = 0; i < result.size(); i++) {
        final Seq<T> seq = result.get(i);
        fileWriter.append(seq.toString());
        fileWriter.append('\t');
        fileWriter.append(CharSeqTools.itoa(result.freq(i)));
        //fileWriter.append('\n');
        fileWriter.append("\n\n");
      }
    }
    finally {
      lock.readLock().unlock();
      fileWriter.close();
    }
  }

  public double codeLength() {
    return result.codeLengthPerChar();
  }

  public static class DictionaryWithStat<T extends Comparable<T>> extends DictionaryBase<T> {
    private final Dictionary<T> dict;
    private final TIntArrayList symbolFreqs;
    private final TIntArrayList parseFreqs;
    private double power = 0;
    private final LongIntMappingAsyncBuilder pairsFreqs;
    private final double minProbability;
    private double totalChars = 0;
    private final FastRandom rng = new FastRandom(0);

    public DictionaryWithStat(Dictionary<T> dict, TIntArrayList initFreqs, double minProbResult) {
      this.dict = dict;
      symbolFreqs = new TIntArrayList(dict.size());
      symbolFreqs.fill(symbolFreqs.size(), dict.size(), 0);
      parseFreqs = initFreqs != null ? initFreqs : new TIntArrayList(dict.size());
      parseFreqs.fill(parseFreqs.size(), dict.size(), 0);
      pairsFreqs = new LongIntMappingAsyncBuilder(AGG_POWER);
      minProbability = minProbResult;
    }

    public void updateSymbol(int index, int freq) {
      // trash double-locking, _pos in symbolFreq is not volatile, won't work in some cases, never ever do like this!
      // in this code I trade performance to certainty of symbolFreqs values.
      // In case it will update the same symbol from different thread the value update will be result of race condition.
      // So, the code is total garbage, but it works fast :)
      // No idea how to rewrite this correctly without dramatic loss of performance
      if (index >= symbolFreqs.size()) {
        synchronized (this) {
          if (index >= symbolFreqs.size()) {
            symbolFreqs.fill(symbolFreqs.size(), index + 1, 0);
            parseFreqs.fill(parseFreqs.size(), index + 1, 0);
          }
        }
      }
      {
        final int val = symbolFreqs.getQuick(index);
        symbolFreqs.setQuick(index, val + freq);
      }
      {
        final int val = parseFreqs.getQuick(index);
        parseFreqs.setQuick(index, val + freq);
      }
      power += freq;
    }

    @Override
    public int search(Seq<T> seq) {
      return dict.search(seq);
    }

    @Override
    public int search(Seq<T> seq, TIntSet excludes) {
      return dict.search(seq, excludes);
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

    public double codeLengthPerChar() {
      double sum = 0;
      for (int i = 0; i < size(); i++) {
        final int freq = freq(i);
        if (freq > 0)
          sum -= freq * log(freq);
      }
      return (sum + power * log(power)) / totalChars;
    }

    private synchronized DictionaryWithStat<T> reduce(int slots, boolean isDynamic) {
      final List<Seq<T>> newDict = new ArrayList<>(size());
      List<StatItem> items = filterStatItems(slots);
      TIntArrayList freqs = new TIntArrayList(items.size());
      double power = items.stream().mapToDouble(i -> i.count).sum();
      double minProbResult = min(1. / size(), MAX_MIN_PROBABILITY);
      for (final StatItem item : items) {
        final double p = (item.count + 1) / (power + size());
        if (parent(item.second) >= 0)
          minProbResult = min(p, minProbResult);
        final Seq<T> symbol = get(item.second);
        newDict.add(symbol);
        freqs.add(item.count);
      }

      //noinspection unchecked
      return createDict(newDict, freqs, isDynamic, minProbResult);
    }

    @NotNull
    private List<StatItem> filterStatItems(int slots) {
      slots += IntStream.range(0, symbolFreqs.size()).filter(s -> parent(s) < 0).count();
      TIntSet excludes = new TIntHashSet();
      IntStream.range(0, size()).filter(id -> parent(id) >= 0 && freq(id) == 0).forEach(excludes::add);
      TIntSet toRemove = new TIntHashSet();
      TIntList changedToRemove = new TIntArrayList();
      TIntDoubleMap updatedFreqs = new TIntDoubleHashMap();

      List<StatItem> items = statItems(excludes);
//
//      while (!items.isEmpty() && (items.size() > slots || items.get(items.size() - 1).score < 0)) {
//        updatedFreqs.clear();
//        changedToRemove.clear();
//        toRemove.clear();
//        { // choose independent items from the end of the sorted variants
//          for (int i = items.size() - 1; i >= 0; i--) {
//            final StatItem item = items.get(i);
//            if (i <= slots && item.score > 0)
//              break;
//            if (parent(item.second) < 0) {
//              slots--;
//              continue;
//            }
//            final Seq<T> candidate = get(item.second);
//            boolean couldBeChanged = false;
//            for (int j = 0; !couldBeChanged && j < items.size(); j++) {
//              Seq<T> a = get(items.get(j).second);
//              if (i == j)
//                continue;
//              couldBeChanged = isSubstring(a, candidate);
//            }
//            if (couldBeChanged)
//              changedToRemove.add(item.second);
//            else
//              toRemove.add(item.second);
//          }
//        }
//
//        if (toRemove.isEmpty() && changedToRemove.isEmpty())
//          break;
//        if (toRemove.isEmpty())
//          toRemove.addAll(changedToRemove.subList((int)(changedToRemove.size() * 0.7), changedToRemove.size()));
//        excludes.addAll(toRemove);
//        toRemove.forEach(id -> {
//          weightParseVariants(dict.get(id), freq(id), symbolFreqs, power, excludes, updatedFreqs);
//          return true;
//        });
//        updatedFreqs.forEachEntry((id, freq) -> {
//          if (toRemove.contains(id))
//            if (symbolFreqs.size() > id)
//              symbolFreqs.setQuick(id, 0);
//          else
//            updateSymbol(id, (int) freq);
//          return true;
//        });
//        power = symbolFreqs.sum();
//        items = statItems(excludes);
//      }
      return items.subList(0, Math.min(items.size(), slots));
    }

    private List<StatItem> statItems(TIntSet excludes) {
      final List<StatItem> items = new ArrayList<>();
      final double codeLength = codeLengthPerChar() * totalChars;
      IntStream.range(0, symbolFreqs.size()).filter(id -> !excludes.contains(id)).forEach(id -> {
        final int count = freq(id);
        final Seq<T> seq = get(id);
        if (seq.length() > 1) {
          final IntSeqBuilder builder = new IntSeqBuilder();
          excludes.add(id);
          weightedParse(seq, symbolFreqs, power, builder, excludes);
          excludes.remove(id);
          final IntSeq parse = builder.build();
          double newPower = power + (parse.length() - 1) * count;
          double codeLengthWOSymbol = codeLength + count * log(count) - power * log(power) + newPower * log(newPower);
          for (int i = 0; i < parse.length(); i++) {
            final int next = parse.intAt(i);
            final int oldFreq = freq(next);
            final int newFreq = oldFreq + count;
            codeLengthWOSymbol -= newFreq * log(newFreq) - (oldFreq > 0 ? oldFreq * log(oldFreq) : 0);
          }
          double score = codeLengthWOSymbol - codeLength;
          if (score > 0)
            items.add(new StatItem(-1, id, score, count));
        }
        else items.add(new StatItem(-1, id, Double.POSITIVE_INFINITY, count));
      });
      items.sort(Comparator.comparingDouble(o -> -o.score)); // rewrite comparator
      return items;
    }

    private <T extends Comparable<T>> T indexOfTwoStr(final Seq<T> first, final Seq<T> second, T betw, int ind) {
      if (ind >= 0 && ind < first.length()) {
        return first.at(ind);
      } else if (ind == first.length()) {
        return betw;
      } else if (ind > first.length() && ind < first.length() + 1 + second.length()) {
        return second.at(ind - first.length() - 1);
      } else {
        return null;
      }
    }



    private int[] pi = new int[10000];
    private Seq<T> nullSymCache;
    private Seq<T> nullSym(Seq<T> s) {
      if (nullSymCache == null) {
        Object nullStr = Array.newInstance(s.elementType(), 1);
        nullSymCache = CharSeqTools.create(nullStr);
      }
      return nullSymCache;
    }
    private boolean isSubstring(final Seq<T> s, final Seq<T> t) {
      if (s.elementType() == char.class)
        return CharSeqTools.indexOf((CharSequence)s, (CharSequence)t) >= 0;
      // t is substr of s
      //Seq<T> superStr = CharSeqTools.concat(t, (Seq<T>)CharSeqTools.create(new null), s);
      if (t.length() > s.length()) {
        return false;
      }
      T symb = null;
      int n = t.length() + 1 + s.length();
      Seq<T> nullSym = nullSym(s);
      Seq<T> concat = CharSeqTools.concat(s, nullSym, t);
      for (int i = 1; i < n; i++) {
        int j = pi[i-1];
        while (j > 0 && concat.at(i) != concat.at(j))
          j = pi[j-1];
        if (concat.at(i) == concat.at(j))
          j++;
        if (j == t.length())
          return true;
        pi[i] = j;
      }
      return false;
    }

    private void printPairs(TLongIntMap oldPairs, TLongIntMap newPairs) {
      for (int first = 0; first < size(); first++) {
        for (int second = 0; second < size(); second++) {
          final long code = (long) first << 32 | second;
          if (oldPairs.get(code) != newPairs.get(code)) {
            System.out.println("\t" + dict.get(first) + "|" + dict.get(second) + ": " + oldPairs.get(code) + " -> " + newPairs.get(code));
          }
        }
      }
    }

    private synchronized DictionaryWithStat<T> expand(int slots, boolean isDynamic) {
      final List<StatItem> items = new ArrayList<>();
      final Set<Seq<T>> known = new HashSet<>();
      alphabet().stream().peek(known::add).map(seq -> new StatItem(-1, index(seq), Double.POSITIVE_INFINITY, freq(index(seq)))).forEach(items::add);
      slots += alphabet().size();
      Vec startWithX = new ArrayVec(symbolFreqs.size());
      Vec endsWithX = new ArrayVec(symbolFreqs.size());
      pairsFreqs.visit((code, freq) -> {
        final int first = (int) (code >>> 32);
        final int second = (int) (code & 0xFFFFFFFFL);

        startWithX.adjust(first, freq);
        endsWithX.adjust(second, freq);
        return true;
      });

      final double totalPairFreqs = VecTools.sum(startWithX);
      pairsFreqs.visit((code, freq) -> {
        final int first = (int) (code >>> 32);
        final int second = (int) (code & 0xFFFFFFFFL);

//        final double pairProbIndependentDirichlet = freq(first) * freq(second) / power / power;
//        final double lambda = pairsFreqs.accumulatedValuesTotal() * pairProbIndependentDirichlet;
//        final double score = MathTools.logPoissonProbability(lambda, freq);

//        final double pAB = freq / totalPairFreqs;
////        final double pBcondA = (freq + 1) / (freqXFirst.get(first) + symbolFreqs.size() - 1);
//        final double pBcondA = freq / startWithX.get(first);
//        double freqA = symbolFreqs.get(first);
//        final double pA = freqA / power;
//        double freqB = symbolFreqs.get(second);
//        final double pB = freqB / power;
//        double score = freq * pBcondA * log(pAB / pA / pB);
//        if (Math.min(freqA, freqB) < 2)
//          score = -1;

        final double ab = freq;
        final double xb = endsWithX.get(second) - freq;
        final double ay = startWithX.get(first) - freq;
        final double xy = totalPairFreqs - ay - xb - ab;

        final Vec dirichletParams = new ArrayVec(ab + 1, ay + 1, xb + 1, xy + 1);
        double score = 0;
        int samplesCount = 10;
        Vec sample = new ArrayVec(dirichletParams.dim());
        for (int i = 0; i < samplesCount; i++) {
          rng.nextDirichlet(dirichletParams, sample);
          double pAB = sample.get(0);
          double pAY = sample.get(1);
          double pXB = sample.get(2);
          score += freq * pAB / (pAY + pAB) * log(pAB / (pAY + pAB) / (pXB + pAB)) / samplesCount;
        }

        final StatItem statItem = new StatItem(first, second, score, freq);
        if (!known.contains(statItem.text())) {
          known.add(statItem.text());
          items.add(statItem);
        }
        return true;
      });

      items.sort(Comparator.comparingDouble(o -> -o.score));
      final List<Seq<T>> newDict = new ArrayList<>();
      final TIntArrayList freqs = new TIntArrayList();
      double minProbResult = minProbability;

      for (final StatItem item : items) {
//        if (item.score > log(0.05))
//          break;
        if (item.score < 0)
          break;
        if (--slots < 0)
          break;

        newDict.add(item.text());
        freqs.add(item.count);
        if (item.first >= 0)
          minProbResult = min(minProbResult, item.count / (double)pairsFreqs.accumulatedValuesTotal());
      }
      //noinspection unchecked
      return createDict(newDict, freqs, isDynamic, minProbResult);
    }

    public boolean enough(double probFound) {
      return power > -log(probFound) / minProbability;
    }

    public void visitAssociations(int start, TIntDoubleProcedure procedure) {
      pairsFreqs.visitRange(((long) start) << 32, ((long) start + 1L) << 32, (a, b) -> procedure.execute((int)(a & 0x7FFFFFFFL), b));
    }

//    static long counter = 0;
    public IntSeq parse(Seq<T> seq) {
      totalChars += seq.length();
      final IntSeq parseResult;
//      boolean debug = ++counter % 10000 == 0;
      {
        IntSeqBuilder builder = new IntSeqBuilder();
        super.weightedParse(seq, parseFreqs, parseFreqs.sum(), builder, new TIntHashSet());
        parseResult = builder.build();
//        if (debug)
//          System.out.println(parseResult.stream().mapToObj(this::get).map(Object::toString).collect(Collectors.joining("|")));
      }
      pairsFreqs.populate(pairsFreq -> {
        final int length = parseResult.length();
        int prev = -1;
        for(int i = 0; i < length; i++) {
          final int symbol = parseResult.intAt(i);
          updateSymbol(symbol, 1);
          if (prev >= 0)
            pairsFreq.adjustOrPutValue((long) prev << 32 | symbol, 1, 1);
          prev = symbol;
        }
      });
      return parseResult;
    }

    private final class StatItem {
      int first;
      int second;
      double score;
      int count;

      private StatItem(final int first, final int second, final double score, final int count) {
        this.first = first;
        this.second = second;
        this.score = score;
        this.count = count;
      }

      @Override
      public String toString() {
        final StringBuilder result = new StringBuilder();
        if (first >= 0)
          result.append(get(first)).append("|");
        result.append(get(second));
        result.append("->(");
        result.append(count);
        result.append(", ").append(score);
        result.append(")");
        return result.toString();
      }

      @Override
      public boolean equals(Object o) {
        if (this == o)
          return true;
        if (o == null || getClass() != o.getClass())
          return false;
        //noinspection unchecked
        StatItem statItem = (StatItem) o;
        return first == statItem.first && second == statItem.second;
      }

      @Override
      public int hashCode() {
        return Objects.hash(first, second);
      }

      public Seq<T> text() {
        return first >= 0 ? CharSeqTools.concat(get(first), get(second)) : get(second);
      }
    }

    private double kl(Vec freqs, TLongIntMap pairFreqs) {
      final Vec freqXFirst = new ArrayVec(freqs.dim());
      pairFreqs.forEachEntry((code, freq) -> {
        freqXFirst.adjust((int) (code >>> 32), freq);
        return true;
      });
      final double totalPairFreqs = VecTools.sum(freqXFirst);
      final double totalFreqs = VecTools.sum(freqs);

      final double result[] = {0};
      pairFreqs.forEachEntry((code, freq) -> {
        final int first = (int) (code >>> 32);
        final int second = (int) (code & 0xFFFFFFFFL);
        final double pAB = freq / totalPairFreqs;
        final double pBcondA = freq / freqXFirst.get(first);
        final double pA = freqs.get(first) / totalFreqs;
        final double pB = freqs.get(second) / totalFreqs;
        result[0] += freq * pBcondA * log(pAB / pA / pB);
        return true;
      });
      return result[0];
    }
  }
}
