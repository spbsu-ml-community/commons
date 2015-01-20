package com.spbsu.commons.math.vectors.impl.mx;

import com.spbsu.commons.math.vectors.Mx;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.seq.ArraySeq;
import com.spbsu.commons.seq.Seq;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:25:41
 */
public class ColsVecArrayMx extends Mx.Stub {
  public final Seq<Vec> vec;

  public ColsVecArrayMx(final Vec[] vec) {
    if (vec.length == 0)
      throw new IllegalArgumentException("Unable to create ColsVecArrayMx from empty array!");
    this.vec = new ArraySeq<Vec>(vec);
  }

  public ColsVecArrayMx(final Seq<Vec> vec) {
    if (vec.length() == 0)
      throw new IllegalArgumentException("Unable to create ColsVecArrayMx from empty array!");
    this.vec = vec;
  }

  @Override
  public double get(final int i, final int j) {
    return vec.at(j).get(i);
  }
  @Override
  public Mx set(final int i, final int j, final double val) {
    vec.at(j).set(i, val);
    return this;
  }

  @Override
  public Mx adjust(final int i, final int j, final double increment) {
    vec.at(j).adjust(i, increment);
    return this;
  }

  @Override
  public Mx sub(final int i, final int j, final int height, final int width) {
    final Vec[] cols = new Vec[width];
    for (int r = 0; r < cols.length; r++) {
      cols[r] = vec.at(j + r).sub(i, height);
    }
    return new ColsVecArrayMx(cols);
  }

  @Override
  public Vec col(final int j) {
    return vec.at(j);
  }

  @Override
  public double get(final int i) {
    return vec.at(i % vec.length()).get(i / vec.length());
  }

  @Override
  public Vec set(final int i, final double val) {
    vec.at(i % vec.length()).set(i / vec.length(), val);
    return this;
  }

  @Override
  public Vec adjust(final int i, final double increment) {
    vec.at(i % vec.length()).adjust(i / vec.length(), increment);
    return this;
  }

  @Override
  public boolean isImmutable() {
    return false;
  }

  @Override
  public int columns() {
    return vec.length();
  }

  @Override
  public int rows() {
    return vec.at(0).dim();
  }
}
