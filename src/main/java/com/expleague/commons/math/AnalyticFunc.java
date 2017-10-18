package com.expleague.commons.math;

import com.expleague.commons.math.vectors.Vec;

/**
 * User: solar
 * Date: 30.11.15
 * Time: 15:04
 */
public interface AnalyticFunc extends FuncC1 {
  double value(double x);
  double gradient(double x);

  abstract class Stub extends FuncC1.Stub implements AnalyticFunc {
    @Override
    public final double value(Vec x) {
      return value(x.get(0));
    }

    @Override
    public Vec gradientTo(Vec x, Vec to) {
      to.set(0, gradient(x.get(0)));
      return to;
    }

    @Override
    public final int dim() {
      return 1;
    }
  }
}
