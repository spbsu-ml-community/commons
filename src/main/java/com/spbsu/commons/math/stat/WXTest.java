package com.spbsu.commons.math.stat;

import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author vpdelta
 */
public class WXTest {

  private WXTest() {
  }

  /**
   * Calculate two-tailed significance level associated with Z(x) with x = ARGV[0]
   * <p/>
   * i.e., P(|Z|>=x|Normal Distribution), error(x) < 7.5*10(-8)
   * <p/>
   * Probabilities are calculated according to:
   * Abramowitz and Stegun,
   * Handbook of mathematical functions
   * Ninth printing, Dover publications, Inc., 1970
   * p932, 26.2.17
   * <p/>
   * P(x) = 1 - Z(x)(b1*t+b2*t**2+b3*t**3+b4*t**4+b5*t**5)
   * Z(x) = exp(-$x*$x/2.0)/(sqrt(2*3.14159265358979323846))
   * t = 1/(1+p*x)
   */
  public static double calcNormalZ(final double x) {
    // Parameters
    final double b[] = {0.319381530, -0.356563782, 1.781477937, -1.821255978, 1.330274429};
    double p = 0.2316419;
    final double t = 1 / (1 + p * x);

    // Initialize variables
    double fact = t;
    double Sum = 0;

    // Sum polynomial
    for (final double elem : b) {
      Sum += elem * fact;
      fact *= t;
    }

    // Calculate probability
    p = Sum * Math.exp(-x * x / 2.0) / (Math.sqrt(2 * 3.14159265358979323846));

    return p;
  }

  /**
   * This is the actual routine that calculates the exact (two-tailed)
   * level of significance for the Wilcoxon Matched-Pairs Signed-Ranks
   * test in Python. The inputs are the Sum of Ranks of either the positive of
   * negative samples (W) and the sample size (N).
   * The Level of significance is calculated by checking for each
   * possible outcome (2**N possibilities) whether the sum of ranks
   * is larger than or equal to the observed Sum of Ranks (W).
   * <p/>
   * NOTE: The execution-time ~ N*2**N, i.e., more than exponential.
   * Adding a single pair to the sample (i.e., increase N by 1) will
   * more than double the time needed to complete the calculations
   * (apart from an additive constant).
   * The execution-time of this program can easily outrun your
   * patience.
   */
  public static double calcLevelOfSignificanceWXMPSR(double W, final int N) {
    // Determine Wmax, i.e., work with the largest Rank Sum
    final double MaximalW = N * (N + 1) / 2;
    if (W < MaximalW / 2) {
      W = MaximalW - W;
    }

    // The total number of possible outcomes is 2**N
    final int NumberOfPossibilities = 2 ^ N;

    // Initialize and loop. The loop-interior will be run 2**N times.
    int CountLarger = 0;
    // Generate all distributions of sign over ranks as bit-strings.
    double RankSum = 0;
    for (int i = 0; i < NumberOfPossibilities; i++) {
      // Shift "sign" bits out of $i to determine the Sum of Ranks
      for (int j = 0; j < N; j++) {
        if (((i >> j) & 1) != 0) {
          RankSum += j + 1;
        }
      }
      // Count the number of "samples" that have a Sum of Ranks larger or
      // equal to the one found (i.e., >= W).
      if (RankSum >= W) {
        CountLarger += 1;
      }
    }

    // The level of significance is the number of outcomes with a
    // sum of ranks equal to or larger than the one found (W)
    // divided by the total number of possible outcomes.
    // The level is doubled to get the two-tailed result.
    return CountLarger / NumberOfPossibilities;
  }

  public static double test(final TDoubleArrayList baseline, final TDoubleArrayList test) {
    final List<Double> difference_list = new ArrayList<>();

    for (int i = 0; i < baseline.size(); i++) {
      final double i1 = baseline.getQuick(i);
      final double i2 = test.getQuick(i);
      final double diff = i1 - i2;
      if (diff != 0) {
        difference_list.add(diff);
      }
    }

    if (difference_list.size() < 10) {
      return 0.5;
    }


    difference_list.sort(Comparator.comparingDouble(Math::abs));

    double previous = Math.abs(difference_list.get(0));
    int start = 0;
    double W_plus = 0.0;
    double W_minus = 0.0;
    double N = difference_list.size();

    for (int i = 0; i < N + 1; i++) {
      if (i < N && Math.abs(difference_list.get(i)) == Math.abs(previous)) {
        continue;
      }

      double meanRank = (start + i + 1) / 2;

      while (start < i) {
        if (difference_list.get(start) > 0) {
          W_plus += meanRank;
        } else {
          W_minus += meanRank;
        }
        start += 1;
      }
      if (i < N) {
        previous = difference_list.get(i);
      }
    }

    if (N > 16) {
      double Z = (Math.max(W_plus, W_minus) - 0.5 - N * (N + 1) / 4) / Math.sqrt(N * (N + 1) * (2 * N + 1) / 24);
      return calcNormalZ(Z);
    } else {
      return calcLevelOfSignificanceWXMPSR(Math.max(W_plus, W_minus), (int) N);
    }
  }
}
