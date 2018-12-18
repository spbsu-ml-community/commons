package com.expleague.commons.math.vectors.impl.vectors;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecIterator;

public class SmartVec extends Vec.Stub {
  private SparseVec sparse;
  private ArrayVec dense;
  private volatile Vec effective;

  @Override
  public double get(int i) {
    return effective().get(i);
  }

  public Vec effective() {
    return effective;
  }

  @Override
  public Vec set(int i, double val) {
    return effective.set(i, val);
  }

  @Override
  public Vec adjust(int i, double increment) {
    return effective.adjust(i, increment);
  }

  @Override
  public VecIterator nonZeroes() {
    return effective.nonZeroes();
  }

  @Override
  public int dim() {
    return effective.dim();
  }

  @Override
  public Vec sub(int start, int len) {
    return effective.sub(start, len);
  }
}
