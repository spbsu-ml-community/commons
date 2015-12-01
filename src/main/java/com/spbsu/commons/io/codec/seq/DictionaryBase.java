package com.spbsu.commons.io.codec.seq;

import com.spbsu.commons.seq.IntSeq;
import com.spbsu.commons.seq.IntSeqBuilder;
import com.spbsu.commons.seq.Seq;
import gnu.trove.list.TIntList;
import gnu.trove.procedure.TObjectDoubleProcedure;

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

  private final ThreadLocal<IntSeqBuilder> builderTL = new ThreadLocal<IntSeqBuilder>() {
    @Override
    protected IntSeqBuilder initialValue() {
      return new IntSeqBuilder();
    }
  };
  static boolean debug = false;

  @Override
  public IntSeq parse(Seq<T> seq, TIntList freqs, double totalFreq) {
    final IntSeqBuilder builder = builderTL.get();
    if (seq.length() < 10) {
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
    return linearParse(seq, builder);
  }

  @Override
  public IntSeq parse(Seq<T> seq) {
    return linearParse(seq, builderTL.get());
  }

  private IntSeq linearParse(Seq<T> seq, IntSeqBuilder builder) {
    Seq<T> suffix = seq;
    while (suffix.length() > 0) {
      int symbol;
      try {
        symbol = search(suffix);
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

  private double exhaustiveParse(Seq<T> seq, TIntList freqs, double totalFreq, IntSeqBuilder builder, double currentLogProbab, double bestLogProBab) {
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

  @Override
  public void visitVariants(Seq<T> arg, TIntList freqs, double totalFreq, TObjectDoubleProcedure<IntSeq> todo) {
    visitVariantsInner(arg, freqs, totalFreq, todo, new IntSeqBuilder(), 0);
  }

  public void visitVariantsInner(Seq<T> seq, TIntList freqs, double totalFreq, TObjectDoubleProcedure<IntSeq> todo, IntSeqBuilder builder, double currentLogProbab) {
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
        double logProbability = currentLogProbab - log(totalFreq + freqs.size() + 1);
        logProbability += freqs.size() > symbol ? log(freqs.get(symbol) + 1) : 0.;
        visitVariantsInner(variant, freqs, totalFreq, todo, builder, logProbability);
        builder.reset();
      }
      while ((symbol = parent(symbol)) >= 0);
    }
    catch (RuntimeException e) {
      if (DICTIONARY_INDEX_IS_CORRUPTED.equals(e.getMessage())) {
        suffix = suffix.sub(1, suffix.length());
        builder.append(-1);
        visitVariantsInner(suffix, freqs, totalFreq, todo, builder, currentLogProbab - 1e-5);
      }
      else throw e;
    }
    finally {
      builder.popMark();
    }
  }
}
