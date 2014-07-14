package com.spbsu.commons.math.vectors.impl.mx;

import com.spbsu.commons.math.vectors.*;
import com.spbsu.commons.math.vectors.impl.basis.MxBasisImpl;
import com.spbsu.commons.math.vectors.impl.idxtrans.SubMxTransformation;
import com.spbsu.commons.math.vectors.impl.vectors.IndexTransVec;
import com.spbsu.commons.seq.ArraySeq;
import com.spbsu.commons.seq.Seq;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:25:41
 */
public class ColsVecArrayMx extends Mx.Stub {
  public final Seq<Vec> vec;

  public ColsVecArrayMx(Vec[] vec) {
    if (vec.length == 0)
      throw new IllegalArgumentException("Unable to create ColsVecArrayMx from empty array!");
    this.vec = new ArraySeq<Vec>(vec);
  }

  public ColsVecArrayMx(Seq<Vec> vec) {
    if (vec.length() == 0)
      throw new IllegalArgumentException("Unable to create ColsVecArrayMx from empty array!");
    this.vec = vec;
  }

  public double get(int i, int j) {
    return vec.at(i).get(j);
  }
  @Override
  public Mx set(int i, int j, double val) {
    vec.at(j).set(i, val);
    return this;
  }

  @Override
  public Mx adjust(int i, int j, double increment) {
    vec.at(j).adjust(i, increment);
    return this;
  }

  @Override
  public Mx sub(int i, int j, int height, int width) {
    final Vec[] cols = new Vec[width];
    for (int r = 0; r < cols.length; r++) {
      cols[r] = vec.at(j + r).sub(i, height);
    }
    return new ColsVecArrayMx(cols);
  }

  @Override
  public Vec col(int j) {
    return vec.at(j);
  }

  @Override
  public double get(int i) {
    return vec.at(i % vec.length()).get(i / vec.length());
  }

  @Override
  public Vec set(int i, double val) {
    vec.at(i % vec.length()).set(i / vec.length(), val);
    return this;
  }

  @Override
  public Vec adjust(int i, double increment) {
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
