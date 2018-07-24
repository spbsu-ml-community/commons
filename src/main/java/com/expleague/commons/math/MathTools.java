package com.expleague.commons.math;

import com.expleague.commons.func.types.ConversionRepository;
import com.expleague.commons.func.types.impl.TypeConvertersCollection;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecIterator;
import com.expleague.commons.math.vectors.VecTools;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import static com.expleague.commons.math.vectors.VecTools.*;
import static java.lang.Math.abs;
import static java.lang.Math.*;

/**
 * @author vp
 */
public abstract class MathTools {
  public static final ConversionRepository CONVERSION = new TypeConvertersCollection(ConversionRepository.ROOT, MathTools.class, "com.expleague.commons.math.io");
  public static final double EPSILON = 1e-6;
  public static final double SQRT3 = sqrt(3.);

  private MathTools() {
  }

  public static int max(final int[] values) {
    int max = values[0];
    for (final int value : values) {
      if (value > max) max = value;
    }
    return max;
  }

  public static double max(final double[] values) {
    double max = values[0];
    for (final double value : values) {
      if (value > max) max = value;
    }
    return max;
  }

  public static double sum(final double[] values) {
    double sum = 0.0;
    for (final double value : values) {
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

  public static final LogFactorial cache = new LogFactorial(1000000);
  public static double logFactorial(final int v) {
    return cache.value(v);
  }

  public static double sigmoid(final double x) {
    return sigmoid(x, 1);
  }

  public static double sigmoid(final double x, final double alpha) {
    return 1.0 / (1 + Math.exp(-alpha * x));
  }

  public static double logFactorialRamanujan(final int v) {
    return (v == 1 || v == 0) ? 0 : v * log(v) - v + log(v * (1 + 4 * v * (1 + 2 * v))) / 6. + log(Math.PI) / 2;
  }

  public static double poissonProbability(final double lambda, final int k) {
    return Math.exp(-lambda + k * log(lambda) - logFactorial(k));
  }

  public static double logPoissonProbability(final double lambda, final int k) {
    return -lambda + k * log(lambda) - logFactorial(k);
  }

  public static double poissonProbabilityFast(final double lambda, final int k) {
    return Math.exp(-lambda + k * log(lambda) - logFactorialRamanujan(k));
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
    final double lambdaLog = log(noiseExpectation);

    double expectation = 0;
    double logSum = 0;
    for (int noiseCount = 0; noiseCount < observationCount; noiseCount++) {
      final double trialProbability = Math.exp(-noiseExpectation + noiseCount * lambdaLog - logSum);
      expectation += (observationCount - noiseCount) * trialProbability;
      logSum += log(noiseCount + 1);
    }
    return expectation;
  }

  public static double triroot(final double a) {
    return signum(a) * pow(abs(a), 1. / 3.);
  }

  public static int quadratic(final double[] x, final double a, final double b, final double c) {
    if (abs(a) > EPSILON) {
      final double D = b * b - 4 * a * c;
      if (D < 0) {
        return 0;
      }
      x[0] = (-b + sqrt(D)) / 2. / a;
      x[1] = (-b - sqrt(D)) / 2. / a;
      return 2;
    }
    else {
      x[0] = -c / b;
      return 1;
    }
  }

  public static int cubic(final double[] x, final double a, double b, double c, double d) {
    if (Math.abs(a) < EPSILON) {
      return quadratic(x, b, c, d);
    }
    b /= a; c /= a; d /= a;
    final double b2 = b * b;
    final double p = - b2 /3. + c;
    final double q = b * ((2./ 27.) * b2 - c / 3.) + d;
    x[0] = x[1] = x[2] = -b/3.;
    final double Q = p * p * p /27. + q * q /4.;
    if (Q > 0) {
      final double sqrtQ = sqrt(Q);
      final double alpha = triroot(-q/2 + sqrtQ);
      final double beta = triroot(-q/2 - sqrtQ);
      x[0] += alpha + beta;
      return 1;
    }
    else if (Q < 0) {
      final double trirootSqrtMQ = pow(sqrt(-Q), 1./3.);
      final double ab = 2 * trirootSqrtMQ;
      final double ambi = 2 * trirootSqrtMQ;
      x[0] += ab;
      x[1] += -ab/2. + ambi * SQRT3/2.;
      x[2] += -ab/2. - ambi * SQRT3/2.;
      return 3;
    }
    else {
      final double ab = 2 * triroot(-q/2);
      x[0] += ab;
      x[1] += -ab/2.;
      return 2;
    }
  }

  public static double sqr(final double v) {
    return v * v;
  }

  public static int sqr(final int v) {
    return v * v;
  }

  public static double meanNaive(final Vec group) {
    return VecTools.sum(group)/group.dim();
  }

  public static double meanJS1(final Vec group, final double sigma) {
    final Vec js = copy(group);
    scale(js, (1-(js.dim() - 2)*sigma*sigma/sqr(norm(group)))/1);

    return VecTools.sum(js)/js.dim();
  }

  public static double meanDropFluctuations(final Vec group) {
    int stop = 100;
    double prevEstimate;
    double nextEstimate = meanNaive(group);
    do {
      prevEstimate = nextEstimate;
      double sum = 0;
      double totalWeight = 0;
      double nzCount = 0;
      final VecIterator it = group.nonZeroes();
      while (it.advance()) {
        final double p = exp(-abs(prevEstimate - it.value()));
        totalWeight += p;
        sum += it.value() * p;
        nzCount++;
      }
      totalWeight += (group.dim() - nzCount) * exp(-abs(prevEstimate - 0.));
      nextEstimate = sum / totalWeight;
//      System.out.println(nextEstimate);
    } while(--stop > 0 && abs(nextEstimate - prevEstimate) > EPSILON);
    return nextEstimate;
  }

  public static int bits(int x) {
    int result = 0;
    while (x != 0) {
      result += (x & 1);
      x >>= 1;
    }
    return result;
  }

  public static double bisection(AnalyticFunc func, double left, double right) {
    return bisection(func, left, right, EPSILON);
  }

  public static double bisection(AnalyticFunc func, double left, double right, double epsilon) {
    if (left == right)
      return left;
    double fLeft = func.value(left);
    if (fLeft == 0)
      return left;
    double fRight = func.value(right);
    if (fRight == 0)
      return right;

    if (fLeft * fRight > 0)
      throw new IllegalArgumentException("Function values for left and right parameters should lay on different sides of 0");

    while (Math.abs(left - right) > epsilon) {
      final double middle = (left + right) / 2.;
      final double fMiddle = func.value(middle);
      if (fMiddle == 0 || middle == right || middle == left)
        return middle;
      if (fLeft * fMiddle > 0) {
        left = middle;
        fLeft = fMiddle;
      }
      else {
        right = middle;
        fRight = fMiddle;
      }
    }
    return left;
  }

  public static double newton(AnalyticFunc func, double left, double right) {
    if (left == right)
      return left;

    double x;
    if (Double.isFinite(left) && Double.isFinite(right))
      x = (left + right)/2;
    else if (Double.isInfinite(left) && Double.isInfinite(right))
      x = 0;
    else if (Double.isInfinite(left))
      x = Double.longBitsToDouble(Double.doubleToLongBits(right) - 1);
    else
      x = Double.longBitsToDouble(Double.doubleToLongBits(left) + 1);;
    while (abs(func.value(x)) > MathTools.EPSILON) {
      double nextX = x - func.value(x) / func.gradient(x);
      if (nextX < left || nextX > right)
        return x;
      x = nextX;
    }
    return x;
  }

  public static boolean locality(double nextX, double x) {
    return x == nextX || Math.abs((nextX - x) / (nextX + x)) < 1e-12;
  }

  public static boolean locality(double nextX, double x, double epsilon) {
    return Math.abs((nextX - x) / (nextX + x)) < epsilon;
  }

  public static double inc(double x) {
    return x > 0 ? Double.longBitsToDouble(Double.doubleToLongBits(x) + 1) : Double.longBitsToDouble(Double.doubleToLongBits(x) - 1);
  }

  public static double dec(double x) {
    return x > 0 ? Double.longBitsToDouble(Double.doubleToLongBits(x) - 1) : Double.longBitsToDouble(Double.doubleToLongBits(x) + 1);
  }

  public static class LogFactorial {
    double cache[];
    public LogFactorial(final int size) {
      double value = 0;
      cache = new double[size];
      cache[0] = 0;
      for (int i = 1; i < size; i++) {
        value += log((double)i);
        cache[i] = value;
      }
    }

    public double value(final int i) {
      return i < cache.length ? cache[i] : Double.POSITIVE_INFINITY;
    }
  }

  public static NumberFormat numberFormatter() {
    final NumberFormat prettyPrint = NumberFormat.getInstance(Locale.US);
    prettyPrint.setMaximumFractionDigits(5);
    prettyPrint.setMinimumFractionDigits(0);
    prettyPrint.setRoundingMode(RoundingMode.HALF_UP);
    prettyPrint.setGroupingUsed(false);
    return prettyPrint;
  }

  public static int combinationsWithRepetition(int n, int k) {
    return combinationsNumber(n + k - 1, k);
  }

  public static int combinationsNumber(int n, int k)
  {
    return factorial(n) / factorial(n - k) / factorial(k);
  }

}

