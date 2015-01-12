package com.spbsu.commons.math.vectors.impl.mx;

import com.spbsu.commons.math.vectors.Mx;
import com.spbsu.commons.math.vectors.MxIterator;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecTools;
import com.spbsu.commons.math.vectors.impl.idxtrans.SubMxTransformation;
import com.spbsu.commons.math.vectors.impl.iterators.MxIteratorImpl;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.math.vectors.impl.vectors.IndexTransVec;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:25:41
 */
public class VecBasedMx extends Mx.Stub {
  public final Vec vec;
  public final int columns;

  public VecBasedMx(final int columns, final Vec vec) {
    this.columns = columns;
    this.vec = vec;
  }

  public VecBasedMx(final int rows, final int columns) {
    this(columns, new ArrayVec(rows * columns));
  }

  public VecBasedMx(final Mx mx) {
    if (mx instanceof VecBasedMx)
      vec = VecTools.copy(((VecBasedMx) mx).vec);
    else vec = mx;
    columns = mx.columns();
  }

  public double get(final int i, final int j) {
    return vec.get(columns * i + j);
  }
  @Override
  public Mx set(final int i, final int j, final double val) {
    vec.set(j + columns * i, val);
    return this;
  }

  @Override
  public Mx adjust(final int i, final int j, final double increment) {
    vec.adjust(j + columns * i, increment);
    return this;
  }

  @Override
  public Mx sub(final int i, final int j, final int height, final int width) {
    return new VecBasedMx(width, new IndexTransVec(vec,
            new SubMxTransformation(columns(), i, j, height, width)));
  }

  @Override
  public Vec row(final int i) {
    return vec.sub(i * columns, columns);
  }

  @Override
  public double get(final int i) {
    return vec.get(i);
  }

  @Override
  public Vec set(final int i, final double val) {
    vec.set(i, val);
    return this;
  }

  @Override
  public Vec adjust(final int i, final double increment) {
    vec.adjust(i, increment);
    return this;
  }

  @Override
  public MxIterator nonZeroes() {
    return new MxIteratorImpl(vec.nonZeroes(), columns);
  }
 
  @Override
  public double[] toArray() {
    return vec.toArray();
  }

  @Override
  public boolean isImmutable() {
    return false;
  }

  @Override
  public int columns() {
    return columns;
  }

  @Override
  public int rows() {
    return vec.dim()/columns;
  }
}
