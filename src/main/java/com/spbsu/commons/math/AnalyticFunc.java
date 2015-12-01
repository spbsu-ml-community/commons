package com.spbsu.commons.math;

import com.spbsu.commons.math.vectors.Vec;

/**
 * User: solar
 * Date: 30.11.15
 * Time: 15:04
 */
public interface AnalyticFunc extends FuncC1 {
  double value(double x);

  abstract class Stub extends FuncC1.Stub implements AnalyticFunc{
    @Override
    public final double value(Vec x) {
      return value(x.get(0));
    }

    @Override
    public final int dim() {
      return 1;
    }
  }
}
