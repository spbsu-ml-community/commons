package com.spbsu.commons.fitting.impl;

import com.spbsu.commons.fitting.Candidate;
import com.spbsu.commons.fitting.Factor;
import com.spbsu.commons.util.Pair;
import gnu.trove.TObjectDoubleHashMap;
import gnu.trove.TObjectDoubleProcedure;

/**
 * @author vp
 */
public class CandidateImpl implements Candidate {
  private final TObjectDoubleHashMap<String> metricValues = new TObjectDoubleHashMap<String>();
  private final Pair<Factor,Double>[] factorValues;

  public CandidateImpl(final Factor[] factors, final double [] values) {
    factorValues = new Pair[factors.length];
    for (int i = 0; i < factorValues.length; i++) {
      factorValues[i] = Pair.create(factors[i], values[i]);
    }
  }

  public void registerMetric(final String metricName, final double value) {
    metricValues.put(metricName, value);
  }

  public Pair[] getFactorValues() {
    return factorValues;
  }

  public Pair[] getMetrics() {
    final Pair[] result = new Pair[metricValues.size()];
    metricValues.forEachEntry(new TObjectDoubleProcedure<String>() {
      int index = 0;

      public boolean execute(String s, double v) {
        result[index++] = Pair.create(s, v);
        return true;
      }
    });
    return result;
  }
}
