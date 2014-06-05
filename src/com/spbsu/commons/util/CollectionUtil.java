package com.spbsu.commons.util;

import com.spbsu.commons.func.TandemComputable;
import com.spbsu.commons.math.vectors.impl.vectors.DVector;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.procedure.TObjectIntProcedure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author vp
 */
public abstract class CollectionUtil {
  private CollectionUtil() {}

  public static <T> double accumulateMap(
    final TObjectDoubleHashMap<T> map,
    final TandemComputable<T,Double,Double> procedure
  ) {
    final DoubleAccumulator<T> accumulator = new DoubleAccumulator<T>(procedure);
    map.forEachEntry(accumulator);
    return accumulator.sum;
  }

  public static <T> double accumulateMap(
    final DVector<T> vector,
    final TandemComputable<T,Double,Double> procedure
  ) {
    final DoubleAccumulator<T> accumulator = new DoubleAccumulator<T>(procedure);
    vector.forEach(accumulator);
    return accumulator.sum;
  }

  public static <T> double accumulateMap(
    final TObjectIntHashMap<T> map,
    final TandemComputable<T,Integer,Double> procedure
  ) {
    final IntegerAccumulator<T> accumulator = new IntegerAccumulator<T>(procedure);
    map.forEachEntry(accumulator);
    return accumulator.sum;
  }

  public static <T> void sortPairListDesc(final List<Pair<T, Double>> list) {
    sortPairList(list, false);
  }
  
  public static <T> void sortPairList(final List<Pair<T, Double>> list, final boolean asc) {
    Collections.sort(list, new Comparator<Pair<T, Double>>() {
      public int compare(Pair<T, Double> o1, Pair<T, Double> o2) {
        final int res = Double.compare(o1.getSecond(), o2.getSecond());
        return asc ? res : -res;
      }
    });
  }

  private static class DoubleAccumulator<T> implements TObjectDoubleProcedure<T> {
    private double sum;
    private final TandemComputable<T,Double,Double> delegate;

    private DoubleAccumulator(final TandemComputable<T,Double,Double> delegate) {
      this.delegate = delegate;
    }

    public boolean execute(final T t, final double v) {
      sum += delegate.compute(t, v);
      return true;
    }
  }

  public static Pair<String, Double>[] getEntries(TObjectDoubleHashMap map) {
    final List<Pair<String, Double>> entries = new ArrayList<Pair<String, Double>>();
    map.forEachEntry(new TObjectDoubleProcedure() {
      public boolean execute(Object o, double v) {
        entries.add(Pair.create(o.toString(), v));
        return true;
      }
    });
    Collections.sort(entries, new Comparator<Pair<String, Double>>() {
      @Override
      public int compare(Pair<String, Double> stringDoublePair, Pair<String, Double> stringDoublePair1) {
        return Double.compare(stringDoublePair1.getSecond(), stringDoublePair.getSecond());
      }
    });
    //noinspection unchecked
    return entries.toArray(new Pair[entries.size()]);
  }

  private static class IntegerAccumulator<T> implements TObjectIntProcedure<T> {
    private double sum;
    private final TandemComputable<T,Integer,Double> delegate;

    private IntegerAccumulator(final TandemComputable<T,Integer,Double> delegate) {
      this.delegate = delegate;
    }

    public boolean execute(final T t, final int v) {
      sum += delegate.compute(t, v);
      return true;
    }
  }
}
