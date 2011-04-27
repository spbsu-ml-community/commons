package com.spbsu.commons.math;

/**
 * @author vp
 */
public abstract class MathTools {
  private MathTools() {
  }

  public static int max(final int[] values) {
    int max = values[0];
    for (int value : values) {
      if (value > max) max = value;
    }
    return max;
  }

  public static double max(final double[] values) {
    double max = values[0];
    for (double value : values) {
      if (value > max) max = value;
    }
    return max;
  }

  public static double sum(final double[] values) {
    double sum = 0.0;
    for (double value : values) {
      sum += value;
    }
    return sum;
  }

  public static int factorial(final int v) {
    int result = 1;
    for (int i = 2; i <= v; i++) {
      result *= i;
    }
    return result;
  }

  public static double logFactorial(final int v) {
    double logSum = 0;
    for (int i = 0; i < v; i++) {
      logSum += Math.log(i + 1);
    }
    return logSum;
  }

  public static double logFactorialRamanujan(final int v) {
    return (v == 1 || v == 0) ? 0 : v * Math.log(v) - v + Math.log(v * (1 + 4 * v * (1 + 2 * v))) / 6. + Math.log(Math.PI) / 2;
  }

  public static double poissonProbability(final double lambda, final int k) {
    return Math.exp(-lambda + k * Math.log(lambda) - logFactorial(k));
  }

  public static double poissonProbabilityFast(final double lambda, final int k) {
    return Math.exp(-lambda + k * Math.log(lambda) - logFactorialRamanujan(k));
  }

  public static double conditionalNonPoissonExpectation(final double noiseExpectation, final int observationCount) {
    double expectation = 0;
    for (int trialOccurenceCount = 1; trialOccurenceCount <= observationCount; trialOccurenceCount++) {
      final double trialProbability = poissonProbability(noiseExpectation, observationCount - trialOccurenceCount);
      expectation += trialOccurenceCount * trialProbability;
    }
    return expectation;
  }

  public static double conditionalNonPoissonExpectationFast(final double noiseExpectation, final int observationCount) {
    final double lambdaLog = Math.log(noiseExpectation);

    double expectation = 0;
    double logSum = 0;
    for (int noiseCount = 0; noiseCount < observationCount; noiseCount++) {
      final double trialProbability = Math.exp(-noiseExpectation + noiseCount * lambdaLog - logSum);
      expectation += (observationCount - noiseCount) * trialProbability;
      logSum += Math.log(noiseCount + 1);
    }
    return expectation;
  }
}

