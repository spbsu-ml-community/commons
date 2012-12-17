package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.*;
import com.spbsu.commons.math.vectors.impl.idxtrans.SubMxTransformation;
import com.spbsu.commons.math.vectors.impl.iterators.MxIteratorImpl;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:25:41
 */
public class VecBasedMx implements Mx {
  public final Vec vec;
  public final int columns;

  public VecBasedMx(int columns, Vec vec) {
    this.columns = columns;
    this.vec = vec;
  }

  public VecBasedMx(int rows, int columns) {
    this(columns, new ArrayVec(rows * columns));
  }

  public VecBasedMx(Mx mx) {
    if (mx instanceof VecBasedMx)
      vec = VecTools.copy(((VecBasedMx) mx).vec);
    else vec = mx;
    columns = mx.columns();
  }

  public double get(int i, int j) {
    return vec.get(columns * i + j);
  }
  @Override
  public Mx set(int i, int j, double val) {
    vec.set(j + columns * i, val);
    return this;
  }

  @Override
  public Mx adjust(int i, int j, double increment) {
    vec.adjust(j + columns * i, increment);
    return this;
  }

  @Override
  public Mx sub(int i, int j, int height, int width) {
    return new VecBasedMx(width, new IndexTransVec(vec,
            new SubMxTransformation(columns(), i, j, width, height),
            new IntBasis(height * width)));
  }

  @Override
  public Vec row(int i) {
    return new IndexTransVec(vec, new SubMxTransformation(columns(), i, 0, columns(), 1), new IntBasis(columns()));
  }

  @Override
  public Vec col(int j) {
    return new IndexTransVec(vec, new SubMxTransformation(columns(), 0, j, 1, rows()), new IntBasis(rows()));
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < rows(); i++) {
      for (int j = 0; j < columns(); j++) {
        builder.append(j > 0 ? "\t" : "");
        builder.append(get(i, j));
      }
      builder.append('\n');
    }
    return builder.toString();
  }

  @Override
  public double get(int i) {
    return vec.get(i);
  }

  @Override
  public Vec set(int i, double val) {
    vec.set(i, val);
    return this;
  }

  @Override
  public Vec adjust(int i, double increment) {
    vec.adjust(i, increment);
    return this;
  }

  @Override
  public MxIterator nonZeroes() {
    return new MxIteratorImpl(vec.nonZeroes(), columns);
  }
 
  @Override
  public MxBasis basis() {
    return new MxBasisImpl(columns, rows());
  }

  @Override
  public int dim() {
    return vec.dim();
  }

  @Override
  public double[] toArray() {
    return new double[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean sparse() {
    return vec.sparse();
  }

  @Override
  public int columns() {
    return columns;
  }

  @Override
  public int rows() {
    return dim()/columns;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof VecBasedMx && (((VecBasedMx)o).columns == columns) && ((VecBasedMx)o).vec.equals(vec);
  }

  @Override
  public int hashCode() {
    return (vec.hashCode() << 1) + columns;
  }
}
