package com.spbsu.commons.fitting;

import com.spbsu.commons.fitting.de.DifferentialEvolution;
import com.spbsu.commons.fitting.de.MinimizationMonitor;
import com.spbsu.commons.fitting.de.MultivariateFunction;
import com.spbsu.commons.util.Holder;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * @author vp
 */
public class DifferentialEvolutionTest extends TestCase {
  public void test1() throws Exception {
    final DifferentialEvolution evolution = new DifferentialEvolution(3);
    final Holder<double[]> minArgs = new Holder<double[]>(null);
    final int precision = 5;
    final double min = evolution.findMinimum(
      new MultivariateFunction() {
        public double evaluate(final double[] x) {
          return x[0] * x[0] + x[1] * x[1] + x[2] * x[2];
        }

        public int getNumArguments() {
          return 3;
        }

        public double getLowerBound(int n) {
          return -10;
        }

        public double getUpperBound(int n) {
          return 10;
        }
      },
      new double[] {precision, -1, 9},
      precision,
      precision,
      new MinimizationMonitor() {
        public void updateProgress(double progress) {
          System.out.println("progress = " + progress);
        }

        @Override
        public void newMinimum(double value, double[] parameterValues) {
          minArgs.setValue(parameterValues);
          System.out.println(Arrays.toString(parameterValues) + "; value = " + value);
        }
      }
    );
    System.out.println("min = " + min);
    assertTrue(min < Math.pow(10, 1 - precision));
  }
}
