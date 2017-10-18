package com.expleague.commons.math.vectors;

import java.util.Arrays;


import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.math.vectors.impl.iterators.SkipVecNZIterator;

/**
 * User: starlight
 * Date: 23.05.14
 */
public class SingleValueVec extends Vec.Stub {
  private double value;
  private final int dim;

  public SingleValueVec(final double value) {
    this(value, 1);
  }

  public SingleValueVec(final double value, final int dim) {
    this.value = value;
    this.dim = dim;
  }

  private void checkIndex(final int i) {
    if (i < 0 || i >= dim) {
      throw new IndexOutOfBoundsException();
    }
  }

  @Override
  public double get(final int i) {
    checkIndex(i);
    return value;
  }

  @Override
  public Vec set(final int i, final double v) {
    checkIndex(i);
    this.value = v;
    return this;
  }

  @Override
  public Vec adjust(final int i, final double increment) {
    throw new UnsupportedOperationException();
  }

  @Override
  public VecIterator nonZeroes() {
    return new SkipVecNZIterator(this);
  }

  @Override
  public int dim() {
    return dim;
  }

  @Override
  public double[] toArray() {
    final double[] result = new double[dim()];
    Arrays.fill(result, value);
    return result;
  }

  @Override
  public Vec sub(final int i, final int i2) {
    if (i < 0 || i2 > 1 || i > i2)
      throw new ArrayIndexOutOfBoundsException();
    if (i == i2)
      return new ArrayVec();
    return new SingleValueVec(value, dim);
  }

  @Override
  public boolean isImmutable() {
    return true;
  }
}
