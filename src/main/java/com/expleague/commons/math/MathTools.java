package com.expleague.commons.math;

import com.expleague.commons.func.types.ConversionRepository;
import com.expleague.commons.func.types.impl.TypeConvertersCollection;
import com.expleague.commons.math.vectors.Mx;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecIterator;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.mx.VecBasedMx;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.seq.IntSeqBuilder;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

import static com.expleague.commons.math.vectors.VecTools.*;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.*;

/**
 * @author vp
 */
public abstract class MathTools {
  public static final ConversionRepository CONVERSION = new TypeConvertersCollection(ConversionRepository.ROOT, MathTools.class, "com.expleague.commons.math.io");
  public static final double EPSILON = 1e-6;
  public static final double SQRT3 = sqrt(3.);
  public static final double GAMMA = 0.5772156649015328606065120900824;

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

  public static int binomial(final int n, final int k) {
    if (n < k)
      return 0;
    if (k == 0 || n == k)
      return 1;
    if (k == 1)
      return n;
    if (n - k < k)
      return binomial(n, n - k);
    return binomial(n - 1, k - 1) + binomial(n - 1, k);
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

  public interface VariantVisitor {
    boolean stage(int i);
    void next();
  }

  public static void visitVariants(int[] varsAtStep, VariantVisitor vv) {
    final int[] current = new int[varsAtStep.length];
    current[varsAtStep.length - 1] = -1;
    boolean last;
    do {
      vv.next();
      last = true;
      for (int j = current.length - 1; j >= 0; j--) {
        if (++current[j] < varsAtStep[j]) {
          last = false;
          boolean accepted = true;
          for (int i = 0; i < current.length; i++) {
            if (!accepted)
              current[i] = varsAtStep[i];
            accepted = accepted && vv.stage(current[i]);
          }
          break;
        }
        else current[j] = 0;
      }
    }
    while (!last);
  }

  public static LongStream bitCountMasks(int bitsCount) {
    Spliterator.OfLong spliterator = new Spliterators.AbstractLongSpliterator(Long.MAX_VALUE,
        Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL) {
      long mask = 0;
      int count = 0;
      int bits = 0;

      @Override
      public boolean tryAdvance(LongConsumer action) {
        if (count == 0) {
          if (++bits == bitsCount)
            return false;
          count = MathTools.binomial(bitsCount, bits) - 1;
          mask = (1 << bits) - 1;
        }
        else {
          int ones = 0;
          long bit = 1 << (bitsCount - 1);
          long mask = this.mask;
          while ((mask & bit) > 0) {
            mask ^= bit;
            bit >>= 1;
            ones++;
          }
          while ((mask & bit) == 0 && bit > 0)
            bit >>= 1;
          mask ^= bit;
          if (bit == 0)
            bit = 1;
          bit <<= 1;
          ones++;
          while (ones-- > 0) {
            mask |= bit;
            bit <<= 1;
          }
          this.count--;
          this.mask = mask;
        }
        action.accept(mask);
        return true;
      }
    };
    return LongStream.concat(LongStream.of(0), LongStream.concat(StreamSupport.longStream(spliterator, false), LongStream.of((1 << (bitsCount + 1)) - 1)));
  }

  public static double maxKnapsack(Vec gain, IntSeq volume, int maxVolume, IntSeqBuilder resultSubset) {
    final Mx state = new VecBasedMx(gain.dim() + 1, maxVolume + 1);
    for (int i = 1; i < volume.length() + 1; i++) {
      final int v_i = volume.intAt(i - 1);
      final double g_i = gain.get(i - 1);
      for (int vol = 0; vol <= maxVolume; vol++) {
        final double gainWithI = v_i <= vol ? state.get(i - 1, vol - v_i) + g_i : 0;
        state.set(i, vol, Math.max(state.get(i - 1, vol), gainWithI));
      }
    }
    if (resultSubset != null) {
      double currentGain = state.get(state.dim() - 1);
      int currentVolume = maxVolume;
      for (int i = gain.dim(); i > 0 && currentGain > 0; i--) {
        final int v_i = volume.intAt(i - 1);
        final double g_i = gain.get(i - 1);
        if (state.get(i-1, currentVolume) != state.get(i, currentVolume)) {
          resultSubset.add(i - 1);
          currentGain -= g_i;
          currentVolume -= v_i;
        }
      }
    }
    return state.get(state.dim() - 1);
  }

  public static double maxTimedKnapsack(Vec gain, IntSeq volume, IntSeq time, IntSeq maxVolume, IntSeqBuilder resultSubset) {
    final List<Mx> states = new ArrayList<>();
    Mx prev = null;
    for (int t = 0; t < maxVolume.length(); t++) {
      final Mx state = new VecBasedMx(gain.dim() + 1, maxVolume.intAt(t) + 1);
      if (prev != null) {
        final Vec firstRow = state.row(0);
        VecTools.assign(firstRow.sub(0, prev.columns()), prev.row(prev.rows() - 1));
        VecTools.fill(firstRow.sub(prev.columns(), firstRow.dim() - prev.columns()), prev.get(prev.dim() - 1));
      }
      for (int i = 1; i < volume.length() + 1; i++) {
        final int v_i = volume.intAt(i - 1);
        final double g_i = gain.get(i - 1);
        for (int vol = 0; vol < state.columns(); vol++) {
          final double gainWithI = v_i <= vol && time.intAt(i - 1) == t ? state.get(i - 1, vol - v_i) + g_i : 0;
          state.set(i, vol, Math.max(state.get(i - 1, vol), gainWithI));
        }
      }
      prev = state;
      states.add(state);
    }
    if (resultSubset != null) {
      int currentVolume = Integer.MAX_VALUE;
      for (int t = states.size() - 1; t >= 0; t--) {
        final Mx state = states.get(t);
        currentVolume = Math.min(state.columns() - 1, currentVolume);
        double currentGain = state.get(state.dim() - 1);
        for (int i = gain.dim(); i > 0 && currentGain > 0; i--) {
          final int v_i = volume.intAt(i - 1);
          final double g_i = gain.get(i - 1);
          if (state.get(i-1, currentVolume) != state.get(i, currentVolume)) {
            resultSubset.add(i - 1);
            currentGain -= g_i;
            currentVolume -= v_i;
          }
        }
      }
    }
    final Mx state = states.get(states.size() - 1);
    return state.get(state.dim() - 1);
  }

  private static final double C_LIMIT = 49;
  private static final double S_LIMIT = 1e-5;
  private static final double F_1_6 = 1d / 6;
  private static final double F_1_30 = 1d / 30;
  private static final double F_1_42 = 1d / 42;
  private static final double F_M1_12 = -1d / 12;
  private static final double F_1_120 = 1d / 120;
  private static final double F_M1_252 = -1d / 252;

  public static double trigamma(double x) {
    if (Double.isNaN(x) || Double.isInfinite(x))
      return x;
    if (x > 0 && x <= S_LIMIT)
      return 1 / sqr(x);
    if (x >= C_LIMIT) {
      final double inv = 1 / sqr(x);
      return 1 / x + inv / 2 + inv / x * (F_1_6 - inv * (F_1_30 + F_1_42 * inv));
    }
    return trigamma(x + 1) + 1 / sqr(x);
  }

  public static double digamma(double x) {
    if (Double.isNaN(x) || Double.isInfinite(x)) {
      return x;
    }

    double digamma = 0;
    if (x < 0) {
      digamma -= Math.PI / Math.tan(Math.PI * x);
      x = 1 - x;
    }

    if (x > 0 && x <= S_LIMIT) {
      return digamma - GAMMA - 1 / x;
    }

    while (x < C_LIMIT) {
      digamma -= 1 / x;
      x += 1;
    }

    final double inv = 1 / (x * x);
    digamma += Math.log(x) - 0.5 / x + inv * (F_M1_12 + inv * (F_1_120 + F_M1_252 * inv));

    return digamma;
  }
}

