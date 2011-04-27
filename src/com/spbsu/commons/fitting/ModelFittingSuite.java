package com.spbsu.commons.fitting;

import com.spbsu.commons.fitting.de.DifferentialEvolution;
import com.spbsu.commons.fitting.de.MinimizationMonitor;
import com.spbsu.commons.fitting.de.MultivariateFunction;
import com.spbsu.commons.fitting.impl.CandidateImpl;
import com.spbsu.commons.util.Holder;
import com.spbsu.commons.util.Pair;

import java.util.Arrays;

/**
 * @author vp
 */
public class ModelFittingSuite {
  public static final int DEFAULT_PRECISION = 2;
  public static final int DEFAULT_MAX_CALLS = 2000;

  private final Model model;
  private final Candidate initialGuess;
  private final int resultPrecision;
  private final int guessPrecision;
  private final int maxCalls;

  public ModelFittingSuite(
    final Model model,
    final Candidate initialGuess,
    final int resultPrecision,
    final int guessPrecision,
    final int maxCalls
  ) {
    this.model = model;
    this.initialGuess = initialGuess;
    this.resultPrecision = resultPrecision;
    this.guessPrecision = guessPrecision;
    this.maxCalls = maxCalls;
  }

  public ModelFittingSuite(
    final Model model,
    final Candidate initialGuess
  ) {
    this(model, initialGuess, DEFAULT_PRECISION, DEFAULT_PRECISION, DEFAULT_MAX_CALLS);
  }

  public Pair<Candidate,Double> fitModelFactors() {
    final Factor<? extends Number>[] factors = model.getFactors();
    final Pair<Factor,Number>[] pairs = initialGuess.getFactorValues();
    final double[] initialGuessValues = new double[pairs.length];
    for (int i = 0; i < pairs.length; i++) {
      initialGuessValues[i] = pairs[i].getSecond().doubleValue();
    }

    final Holder<Candidate> bestCandidate = new Holder<Candidate>(initialGuess);

    final Holder<Candidate> current = new Holder<Candidate>(initialGuess);
    final DifferentialEvolution evolution = new DifferentialEvolution(factors.length);
    evolution.maxFun = maxCalls;

    final MultivariateFunction function = new MultivariateFunction() {
      public double evaluate(final double[] x) {
        for (int i = 0; i < factors.length; i++) {
          ((Factor<Number>) factors[i]).setValue((Number) x[i]); // it will join
        }
        final CandidateImpl candidate = new CandidateImpl(factors, x);
        current.setValue(candidate);
        return model.evaluate(candidate);
      }

      public int getNumArguments() {
        return factors.length;
      }

      public double getLowerBound(final int n) {
        return factors[n].getLowBound().doubleValue();
      }

      public double getUpperBound(final int n) {
        return factors[n].getUpBound().doubleValue();
      }
    };


    final double min = evolution.findMinimum(
      function,
      initialGuessValues,
      resultPrecision,
      guessPrecision,
      new MinimizationMonitor() {
        public void updateProgress(final double progress) {}
        @Override
        public void newMinimum(final double value, final double[] parameterValues) {
          final Candidate candidate = current.getValue();
          bestCandidate.setValue(candidate);

          System.out.println(Arrays.toString(parameterValues) + "; value = " + value);
          final Pair<String,Double>[] metrics = candidate.getMetrics();
          for (final Pair metric : metrics) {
            System.out.println(metric.getFirst() + " = " + metric.getSecond());
          }
        }
      }
    );

    return Pair.create(bestCandidate.getValue(), min);
  }
}
