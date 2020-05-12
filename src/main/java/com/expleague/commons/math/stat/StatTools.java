package com.expleague.commons.math.stat;

import com.expleague.commons.math.MathTools;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;

import static com.expleague.commons.math.MathTools.sqr;

/**
 * Created by noxoomo on 01/06/15.
 */
public class StatTools {
  public static double variance(Vec x) {
    final double sum = VecTools.sum(x);
    final double sum2 = VecTools.sum2(x);
    final double weight = x.dim();
    return sum2 / weight - MathTools.sqr(sum / weight);
  }

  public static double mean(Vec x) {
    return VecTools.sum(x) / x.dim();
  }
}
