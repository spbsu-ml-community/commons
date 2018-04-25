package com.expleague.commons.io.codec.seq;

import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.seq.IntSeqBuilder;
import com.expleague.commons.seq.Seq;
import gnu.trove.list.TIntList;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.set.TIntSet;

import java.util.*;

import static java.lang.Math.exp;
import static java.lang.Math.log;

/**
 * User: solar
 * Date: 22.05.14
 * Time: 16:18
 */
public abstract class DictionaryBase<T extends Comparable<T>> implements Dictionary<T> {
  public static final String DICTIONARY_INDEX_IS_CORRUPTED = "Dictionary index is corrupted!";

  protected DictionaryBase() {
  }

  static boolean debug = false;

  @Override
  public IntSeq parse(Seq<T> seq, TIntList freqs, double totalFreq) {
    final IntSeqBuilder builder = new IntSeqBuilder();
    final double logProBab = weightedParse(seq, freqs, totalFreq, builder);
    final IntSeq result = builder.build();
    if (logProBab > 0 || debug) {
      synchronized (System.out) {
        System.out.print(seq.toString() + " ->");
        for (int i = 0; i < result.length(); i++) {
          final int symbol = result.intAt(i);
          if (symbol >= 0)
            System.out.print(" " + get(symbol));
          else
            System.out.print("##unknown##");
        }
        System.out.println(" " + logProBab);
      }
    }
    return result;
  }

  public IntSeq parseEx(Seq<T> seq, TIntList freqs, double totalFreq) {
    final IntSeqBuilder builder = new IntSeqBuilder();
    final double logProBab = exhaustiveParse(seq, freqs, totalFreq, builder, 0, Double.NEGATIVE_INFINITY);
    final IntSeq result = builder.build();
    if (logProBab > 0 || debug) {
      synchronized (System.out) {
        System.out.print(seq.toString() + " ->");
        for (int i = 0; i < result.length(); i++) {
          final int symbol = result.intAt(i);
          if (symbol >= 0)
            System.out.print(" " + get(symbol));
          else
            System.out.print("##unknown##");
        }
        System.out.println(" " + logProBab);
      }
    }
    return result;
  }

  @Override
  public IntSeq parse(Seq<T> seq) {
    return linearParse(seq, new IntSeqBuilder(), null);
  }

  @Override
  public IntSeq parse(Seq<T> seq, TIntSet excludes) {
    return linearParse(seq, new IntSeqBuilder(), excludes);
  }

  protected IntSeq linearParse(Seq<T> seq, IntSeqBuilder builder, TIntSet excludes) {
    Seq<T> suffix = seq;
    while (suffix.length() > 0) {
      int symbol;
      try {
        symbol = search(suffix, excludes);
        suffix = suffix.sub(get(symbol).length(), suffix.length());
      }
      catch (RuntimeException e) {
        if (DICTIONARY_INDEX_IS_CORRUPTED.equals(e.getMessage())) {
          symbol = -1;
          suffix = suffix.sub(1, suffix.length());
        }
        else throw e;
      }
      builder.add(symbol);
    }
    return builder.build();
  }

  protected double exhaustiveParse(Seq<T> seq, TIntList freqs, double totalFreq, IntSeqBuilder builder, double currentLogProbab, double bestLogProBab) {
    if (seq.length() == 0)
       return currentLogProbab;
    Seq<T> suffix = seq;
    int symbol;
    builder.pushMark();
    try {
      double bestProbability = Double.NEGATIVE_INFINITY;
      IntSeq bestSeq = null;
      symbol = search(suffix);

      do {
        builder.append(symbol);
        final Seq<T> variant = suffix.sub(get(symbol).length(), suffix.length());
        double logProbability = currentLogProbab - log(totalFreq + freqs.size() + 1);
        logProbability += freqs.size() > symbol ? log(freqs.get(symbol) + 1) : 0.;
        if (logProbability > bestLogProBab) {
          if (variant.length() > 0)
            logProbability = exhaustiveParse(variant, freqs, totalFreq, builder, logProbability, bestLogProBab);
          if (logProbability > bestProbability) {
            bestProbability = logProbability;
            bestSeq = builder.build();
          }
        }
        builder.reset();
      }
      while ((symbol = parent(symbol)) >= 0);
      builder.addAll(bestSeq);
      return bestProbability;
    }
    catch (RuntimeException e) {
      if (DICTIONARY_INDEX_IS_CORRUPTED.equals(e.getMessage())) {
        suffix = suffix.sub(1, suffix.length());
        builder.append(-1);
        return exhaustiveParse(suffix, freqs, totalFreq, builder, currentLogProbab - 1e-5, bestLogProBab);
      }
      else throw e;
    }
    finally {
      builder.popMark();
    }
  }

  protected double weightedParse(Seq<T> seq, TIntList freqs, double totalFreq, IntSeqBuilder builder) {
    int len = seq.length();
    double[] score = new double[len + 1];
    Arrays.fill(score, Double.NEGATIVE_INFINITY);
    score[0] = 0;
    int[] symbols = new int[len + 1];

    for (int pos = 0; pos < len; pos++) {
      Seq<T> suffix = seq.sub(pos, len);
      int sym = search(suffix);
      do {
        int symLen = get(sym).length();
        double symLogProb = (freqs.size() > sym ? log(freqs.get(sym) + 1) : 0) - log(totalFreq + size());

        if (score[symLen + pos] < score[pos] + symLogProb) {
          score[symLen + pos] = score[pos] + symLogProb;
          symbols[symLen + pos] = sym;
        }
      }
      while ((sym = parent(sym)) >= 0);
    }
    int[] solution = new int[len + 1];
    int pos = len;
    int index = 0;
    while (pos > 0) {
      int sym = symbols[pos];
      solution[len - (++index)] = sym;
      pos -= get(sym).length();
    }
    for (int i = 0; i < index; i++) {
      builder.append(solution[len - index + i]);
    }
    return score[len];
  }

  protected double weightedParse(Seq<T> seq, TIntList freqs, double totalFreq, IntSeqBuilder builder, TIntSet excludes) {
    int len = seq.length();
    double[] score = new double[len + 1];
    Arrays.fill(score, Double.NEGATIVE_INFINITY);
    score[0] = 0;
    int[] symbols = new int[len + 1];

    for (int pos = 0; pos < len; pos++) {
      Seq<T> suffix = seq.sub(pos, len);
      int sym = search(suffix, excludes);
      do {
        int symLen = get(sym).length();
        double symLogProb = (freqs.size() > sym ? log(freqs.get(sym) + 1) : 0) - log(totalFreq + size());

        if (score[symLen + pos] < score[pos] + symLogProb) {
          score[symLen + pos] = score[pos] + symLogProb;
          symbols[symLen + pos] = sym;
        }
      }
      while ((sym = parent(sym)) >= 0);
    }
    int[] solution = new int[len + 1];
    int pos = len;
    int index = 0;
    while (pos > 0) {
      int sym = symbols[pos];
      solution[len - (++index)] = sym;
      pos -= get(sym).length();
    }
    for (int i = 0; i < index; i++) {
      builder.append(solution[len - index + i]);
    }
    return score[len];
  }

  protected Map<Integer, Double> weightedMultiParse(Seq<T> seq, TIntList freqs, double totalFreq, TIntSet excludes) {
    Map<Integer, Double> parseProb = new HashMap<>();
    int len = seq.length();
    int[] numParseEnds = new int[len + 1];
    for (int i = 0; i <= len; i++) {
      numParseEnds[i] = numOfParseVatiants(seq.sub(i, len), excludes);
    }
    for (int i = 0; i <= len; i++) {
      int numParseStart = numOfParseVatiants(seq.sub(0, i), excludes);
      for (int j = i + 1; j <= len; j++) {
        Seq<T> subs = seq.sub(i, j);
        int sym = search(subs, excludes);
        if (sym >= 0 && get(sym).length() == subs.length()) {
          double symProb = (freqs.size() > sym ? (freqs.get(sym) + 1) : 0) / (totalFreq + size()); // - excludes.size());
          //count[i][j] = numParseStart * numParseEnds[j];
          parseProb.put(sym, parseProb.getOrDefault(sym, 0.0) + symProb * numParseStart * numParseEnds[j]);
        }
      }
    }
    double sum = parseProb.values().stream().mapToDouble(x -> x).sum();
    double norm = sum * numParseEnds[0];
    for (Integer key : parseProb.keySet()) {
      parseProb.put(key, parseProb.getOrDefault(key, 0.0) / norm);
    }
    /*System.out.println(seq + ": " + sum + ", " + numParseEnds[0]);
    parseProb.forEach((key, value) -> System.out.print("(" + get(key) + " - " + value + ") "));
    System.out.println();*/
    return parseProb;
    /*int[] count = new int[len + 1];
    Arrays.fill(count, 0);
    count[0] = 1;
    for (int pos = 0; pos < len; pos++) {
      Seq<T> suffix = seq.sub(pos, len);
      int sym = search(suffix, excludes);
      do {
        int symLen = get(sym).length();
        double symLogProb = (freqs.size() > sym ? log(freqs.get(sym) + 1) : 0) - log(totalFreq + size()); // - excludes.size());
        parseProb.put(sym, parseProb.getOrDefault(sym, 0.0) + symLogProb * count[pos]);
        if (pos + symLen < len) {
          count[pos + symLen] += 1;
        }
      }
      while ((sym = parent(sym)) >= 0);
    }*/
    //ParseTree tree = new ParseTree(seq, freqs, totalFreq, excludes);
    //System.out.println("in weighted multi parse");
    //return tree.wordsProbs();
    /*int len = seq.length();
    Deque<Pair<Integer, List<Integer>>> parseDeque = new LinkedList<>();
    List<Pair<List<Integer>, Double>> parseResults = new ArrayList<>();
    parseDeque.add(new Pair<>(0, new ArrayList<>()));
    while (parseDeque.size() > 0) {
      Pair<Integer, List<Integer>> pair = parseDeque.poll();
      Seq<T> suffix = seq.sub(pair.getFirst(), len);
      int sym = search(suffix, excludes);
      do {
        List<Integer> temp = new ArrayList<>(pair.second);
        temp.add(sym);
        if (pair.first + get(sym).length() == len) {
          double score = 0;
          for (Integer id : temp) {
            score += (freqs.size() > id ? log(freqs.get(id) + 1) : 0) - log(totalFreq + size());
          }
          parseResults.add(new Pair<>(temp, score));
          break;
        } else {
          parseDeque.add(new Pair<>(pair.first + get(sym).length(), temp));
        }
      }
      while ((sym = parent(sym)) >= 0);
    }
    return parseResults;*/
    /*Deque<Integer> posDeque = new LinkedList<>();
    Map<Integer, Integer> parseFreqs = new HashMap<>(freqs.size());
    while (posDeque.size() > 0) {
      int pos = posDeque.poll();
      int sym = search(seq.sub(pos, len), excludes);
      do {
        parseFreqs.put(sym, parseFreqs.getOrDefault(sym, 0) + 1);
        posDeque.add(pos + get(sym).length());
      }
      while ((sym = parent(sym)) >= 0);
    }*/

    /*double[] score = new double[len + 1];
    Arrays.fill(score, Double.NEGATIVE_INFINITY);
    score[0] = 0;
    int[] symbols = new int[len + 1];

    for (int pos = 0; pos < len; pos++) {
      Seq<T> suffix = seq.sub(pos, len);
      int sym = search(suffix, excludes);
      do {
        int symLen = get(sym).length();
        double symLogProb = (freqs.size() > sym ? log(freqs.get(sym) + 1) : 0) - log(totalFreq + size());

        if (score[symLen + pos] < score[pos] + symLogProb) {
          score[symLen + pos] = score[pos] + symLogProb;
          symbols[symLen + pos] = sym;
        }
      }
      while ((sym = parent(sym)) >= 0);
    }
    int[] solution = new int[len + 1];
    int pos = len;
    int index = 0;
    while (pos > 0) {
      int sym = symbols[pos];
      solution[len - (++index)] = sym;
      pos -= get(sym).length();
    }
    for (int i = 0; i < index; i++) {
      builder.append(solution[len - index + i]);
    }
    return score[len];*/
  }

  private int numOfParseVatiants(Seq<T> seq, TIntSet excludes) {
    int len = seq.length();
    int[] count = new int[len + 1];
    Arrays.fill(count, 0);
    count[0] = 1;
    for (int pos = 0; pos < len; pos++) {
      Seq<T> suffix = seq.sub(pos, len);
      int sym = search(suffix, excludes);
      do {
        int symLen = get(sym).length();
        if (pos + symLen <= len) {
          count[pos + symLen] += count[pos];
        }
      }
      while ((sym = parent(sym)) >= 0);
    }
    return count[len];
  }

  private class ParseTree {
    private double score;
    private Map<Integer, ParseTree> children;

    public ParseTree(Seq<T> seq, TIntList freqs, double totalFreq, TIntSet excludes) {
      this(seq, freqs, totalFreq, excludes, 0);
    }

    private ParseTree(Seq<T> seq, TIntList freqs, double totalFreq, TIntSet excludes, double logProb) {
      if (seq.length() == 0) {
        score = exp(logProb);
        //System.out.println("score in leaf: " + score);
        return;
      }
      children = new HashMap<>();
      int sym = DictionaryBase.this.search(seq, excludes);
      do {
        //System.out.println(seq + " - " + get(sym) + " = " + seq.sub(get(sym).length(), seq.length()));
        children.put(sym,
                new ParseTree(seq.sub(get(sym).length(), seq.length()), freqs, totalFreq, excludes,
                        logProb + (freqs.size() > sym ? log(freqs.get(sym) + 1) : 0) - log(totalFreq + size() - excludes.size())));
        score += children.get(sym).score;
      }
      while ((sym = parent(sym)) >= 0);
    }

    public Map<Integer, Double> wordsProbs() {
      Map<Integer, Double> probs = new HashMap<>();
      wordsProbs(probs);
      return probs;
    }

    private void wordsProbs(Map<Integer, Double> probs) {
      if (children == null) {
        return;
      }
      for (Map.Entry<Integer, ParseTree> entry : children.entrySet()) {
        probs.put(entry.getKey(), probs.getOrDefault(entry.getKey(), 0.0) + entry.getValue().score);
        entry.getValue().wordsProbs(probs);
      }
    }

    public double getScore() {
      return score;
    }
  }

  @Override
  public int search(Seq<T> seq) {
    return search(seq, null);
  }

  @Override
  public void visitVariants(Seq<T> arg, TIntList freqs, double totalFreq, TObjectDoubleProcedure<IntSeq> todo) {
    visitVariantsInner(arg, freqs, todo, new IntSeqBuilder(), 0);
  }

  public void visitVariantsInner(Seq<T> seq, TIntList freqs, TObjectDoubleProcedure<IntSeq> todo, IntSeqBuilder builder, double currentLogProbab) {
    if (seq.length() == 0) {
      todo.execute(builder.buildAll(), currentLogProbab);
      return;
    }
    Seq<T> suffix = seq;
    int symbol;
    builder.pushMark();
    try {
      symbol = search(suffix);

      do {
        builder.append(symbol);
        final Seq<T> variant = suffix.sub(get(symbol).length(), suffix.length());
        double logProbability = currentLogProbab;
        logProbability += freqs.size() > symbol ? log(freqs.get(symbol) + 1) : 0.;
        visitVariantsInner(variant, freqs, todo, builder, logProbability);
        builder.reset();
      }
      while ((symbol = parent(symbol)) >= 0);
    }
    catch (RuntimeException e) {
      if (DICTIONARY_INDEX_IS_CORRUPTED.equals(e.getMessage())) {
        suffix = suffix.sub(1, suffix.length());
        builder.append(-1);
        visitVariantsInner(suffix, freqs, todo, builder, currentLogProbab - 1e-5);
      }
      else throw e;
    }
    finally {
      builder.popMark();
    }
  }
}
