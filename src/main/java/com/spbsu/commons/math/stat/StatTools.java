package com.spbsu.commons.math.stat;

import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecTools;

import static com.spbsu.commons.math.MathTools.sqr;

/**
 * Created by noxoomo on 01/06/15.
 */
public class StatTools {

  public static double variance(Vec x) {
    final double sum = VecTools.sum(x);
    final double sum2 = VecTools.sum2(x);
    final double weight = x.dim();
    return sum2 / weight - sqr(sum / weight);
  }
}
