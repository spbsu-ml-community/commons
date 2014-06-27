package com.spbsu.commons.math;

import com.spbsu.commons.math.vectors.Mx;
import com.spbsu.commons.math.vectors.MxTools;
import com.spbsu.commons.math.vectors.impl.mx.VecBasedMx;
import com.spbsu.commons.util.logging.Interval;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * @author vp
 */
public class MathToolsTest extends TestCase {
  private static final double EPS = 1E-3;

  public void testLogFactorialConsistency() throws Exception {
    for (int i = 1; i < 100; i++) {
      final double v1 = MathTools.logFactorial(i);
      final double v2 = MathTools.logFactorialRamanujan(i);
      assertEqualsWithPrecision(v1, v2);
    }
    assertEquals(0., MathTools.logFactorialRamanujan(0));
    assertEquals(0., MathTools.logFactorialRamanujan(1));

    assertEquals(0., MathTools.logFactorial(0));
    assertEquals(0., MathTools.logFactorial(1));
  }

  public void testPoissonProbabilityConsistency() throws Exception {
    final double eps = 1E-3;
    for (double lambda = 0.1; lambda < 10; lambda += 0.1) {
      for (int i = 1; i < 100; i++) {
        final double v1 = MathTools.poissonProbability(lambda, i);
        final double v2 = MathTools.poissonProbabilityFast(lambda, i);
        assertEqualsWithPrecision(v1, v2);
      }
    }
  }

  public void testFactorialPerformace() throws Exception {
    final int k = 10000;

    // log
    Interval.start();
    final int count = 1000;
    for (int q = 0; q < count; q++) {
      Math.exp(MathTools.logFactorial(k));
    }
    Interval.stopAndPrint();

    // def
    Interval.start();
    for (int q = 0; q < count; q++) {
      MathTools.factorial(k);
    }
    Interval.stopAndPrint();

    // Ramanujan
    Interval.start();
    for (int q = 0; q < count; q++) {
      Math.exp(MathTools.logFactorialRamanujan(k));
    }
    Interval.stopAndPrint();
  }

  public void testConditionalNonPoissonNoiseExpectation() throws Exception {
    assertEquals(0., MathTools.conditionalNonPoissonExpectation(100, 0));
    assertEquals(MathTools.poissonProbability(1, 0), MathTools.conditionalNonPoissonExpectation(1, 1));
    assertEquals(
      2 * MathTools.poissonProbability(1, 0) + MathTools.poissonProbability(1, 1), 
      MathTools.conditionalNonPoissonExpectation(1, 2)
    );
    assertEquals(
      MathTools.poissonProbability(2, 0), 
      MathTools.conditionalNonPoissonExpectation(2, 1)
    );
    assertEquals(
      2 * MathTools.poissonProbability(2, 0) + MathTools.poissonProbability(2, 1), 
      MathTools.conditionalNonPoissonExpectation(2, 2)
    );
    assertEquals(
      3 * MathTools.poissonProbability(2, 0) + 2 * MathTools.poissonProbability(2, 1) + MathTools.poissonProbability(2, 2), 
      MathTools.conditionalNonPoissonExpectation(2, 3)
    );
  }

  public void testConditionalNonPoissonNoiseExpectationFast() throws Exception {
    assertEquals(0., MathTools.conditionalNonPoissonExpectationFast(100, 0));
    assertEquals(MathTools.conditionalNonPoissonExpectation(1, 1), MathTools.conditionalNonPoissonExpectationFast(1, 1));
    assertEquals(
      MathTools.conditionalNonPoissonExpectation(1, 2),
      MathTools.conditionalNonPoissonExpectationFast(1, 2)
    );
    assertEquals(
      MathTools.conditionalNonPoissonExpectation(2, 1),
      MathTools.conditionalNonPoissonExpectationFast(2, 1)
    );
    assertEquals(
      MathTools.conditionalNonPoissonExpectation(2, 2),
      MathTools.conditionalNonPoissonExpectationFast(2, 2)
    );
    assertEquals(
      MathTools.conditionalNonPoissonExpectation(2, 3),
      MathTools.conditionalNonPoissonExpectationFast(2, 3)
    );
  }

  public void testConditionalNonPoissonNoiseExpectationFastPerf() throws Exception {
    final int runCount = 1000;
    
    Interval.start();
    for (int i = 0; i < runCount; i++) {
      MathTools.conditionalNonPoissonExpectation(124.324, 222);
    }
    Interval.stopAndPrint();

    Interval.start();
    for (int i = 0; i < runCount; i++) {
      MathTools.conditionalNonPoissonExpectationFast(124.324, 222);
    }
    Interval.stopAndPrint();
  }

  public static void assertEqualsWithPrecision(double v1, double v2) {
    if (v1 != v2) {
      final double ratio = Math.abs(v1 - v2) / Math.min(v1, v2);
      assertTrue(ratio < EPS);
    }
  }

  public void testLQDecompositionFail() throws FileNotFoundException {
    Scanner scanner = new Scanner(new File("commons/tests/data/math/badMx.txt"));
    int n = scanner.nextInt();
    Mx mx = new VecBasedMx(n, n);
    Mx l = new VecBasedMx(n, n);
    Mx q = new VecBasedMx(n, n);
    double eps = 1e-3;
    for (int i = 0; i < n; i++)
      for (int j = 0; j < n; j++)
        mx.set(i, j, Double.parseDouble(scanner.next()));
    MxTools.householderLQ(mx, l, q);
    for (int i = 0; i < n; i++)
      for (int j = i + 1; j < n; j++)
        if (Math.abs(l.get(i, j)) > eps)
          System.out.println("Bad L = " + l.get(i, j));
    Mx qq = MxTools.multiply(q, MxTools.transpose(q));
    Mx lq = MxTools.multiply(l, MxTools.transpose(q));
    for (int i = 0; i < n; i++)
      for (int j = 0; j < n; j++) {
        if (i != j && Math.abs(qq.get(i, j)) > eps)
          System.out.println("Bad Q = " + q.get(i, j));
        if (i == j && Math.abs(qq.get(i, j) - 1) > eps)
          System.out.println("Bad Q = " + q.get(i, j));
      }
    for (int i = 0; i < n; i++)
      for (int j = 0; j < n; j++)
        if (Math.abs(lq.get(i, j) - mx.get(i, j)) > eps)
          System.out.println("Bad LQ, diff = " + (lq.get(i, j) - mx.get(i, j)));

  }

}
