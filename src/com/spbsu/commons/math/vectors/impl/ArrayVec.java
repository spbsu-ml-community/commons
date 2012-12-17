package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.*;
import com.spbsu.commons.math.vectors.impl.iterators.ArrayVecNZIterator;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:25:41
 */
public class ArrayVec implements Vec {
  public final double[] values;
  private final IntBasis basis;

  public ArrayVec(double... values) {
    this.values = values;
    basis = new IntBasis(values.length);
  }

  public ArrayVec(int dim) {
    this.values = new double[dim];
    basis = new IntBasis(dim);
  }

  @Override
  public int dim() {
    return values.length;
  }

  @Override
  public double[] toArray() {
    return values;
  }

  @Override
  public boolean sparse() {
    return false;
  }

  @Override
  public double get(int i) {
    return values[i];
  }

  @Override
  public Vec set(int i, double val) {
    values[i] = val;
    return this;
  }

  @Override
  public Vec adjust(int i, double increment) {
    values[i] += increment;
    return this;
  }

  @Override
  public VecIterator nonZeroes() {
    return new ArrayVecNZIterator(this);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Vec && VecTools.equals(this, (Vec) o);
  }

  @Override
  public int hashCode() {
    return VecTools.hashCode(this);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < dim(); i++)
      builder.append(i > 0 ? " " : "").append(get(i));
    return builder.toString();
  }

  @Override
  public Basis basis() {
    return basis;
  }

}
