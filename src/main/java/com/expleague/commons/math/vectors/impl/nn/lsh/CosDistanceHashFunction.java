package com.expleague.commons.math.vectors.impl.nn.lsh;

import com.expleague.commons.func.HashFunction;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.random.FastRandom;

class CosDistanceHashFunction implements HashFunction<Vec> {
  private final Vec w;
  CosDistanceHashFunction(int dim, FastRandom rng) {
    w = VecTools.fillGaussian(new ArrayVec(dim), rng);
  }

  @Override
  public int hash(Vec v) {
    return VecTools.multiply(v, w) > 0 ? 0 : 1;
  }

  @Override
  public int bits() {
    return 1;
  }
}
