package com.spbsu.commons.random;

import com.spbsu.commons.math.vectors.MxTools;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.math.vectors.Mx;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecTools;

import java.util.Random;

/**
 * User: solar
 * Date: 13.08.12
 * Time: 16:52
 */
public class GaussianRandomVec {
  public final Mx L;
  public final Vec m;
  private final Random random;

  public GaussianRandomVec(Vec m, Mx sigma, Random random) {
    this.m = m;
    this.random = random;
    L = MxTools.choleskyDecomposition(sigma);
  }

  public GaussianRandomVec(Vec m, Mx sigma) {
    this(m, sigma, new FastRandom());
  }

  public Vec next() {
    final int dim = m.dim();
    Vec gaussian = new ArrayVec(dim);
    for (int i = 0; i < dim; i++) {
      gaussian.set(i, random.nextGaussian());
    }
    Vec result = MxTools.multiply(L, gaussian);
    VecTools.append(result, m);
    return result;
  }
}
