package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.*;
import com.spbsu.commons.math.vectors.impl.iterators.SkipVecNZIterator;
import com.spbsu.commons.util.ArrayPart;
import com.spbsu.commons.util.ArrayTools;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:25:41
 */
public class ArrayVec extends ArrayPart<double[]> implements Vec {
  public ArrayVec(double... values) {
    super(values);
  }

  public ArrayVec(int dim) {
    super(new double[dim]);
  }

  public ArrayVec(double[] values, int offset, int length) {
    super(values, offset, length);
  }

  @Override
  public int dim() {
    return length;
  }

  public void add(ArrayPart<double[]> right) {
    ArrayTools.add(array, start, right.array, right.start, length);
  }

  public void fill(double v) {
    ArrayTools.fill(array, start, length, v);
  }

  public void scale(double s) {
    ArrayTools.mul(array, start, length, s);
  }

  public void scale(ArrayVec other) {
    ArrayTools.scale(array, start, other.array, other.start, length);
  }

  public double mul(ArrayVec other) {
    return ArrayTools.mul(array, start, other.array, other.start, length);
  }

  public double l2(ArrayVec other) {
    return ArrayTools.l2(array, start, other.array, other.start, length);
  }

  public int max() {
    int maxIndex = -1;
    double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < length; i++) {
      final double v = array[start + i];
      if (max < v) {
        maxIndex = i;
        max = v;
      }
    }
    return maxIndex;
  }

  public int min() {
    int maxIndex = -1;
    double min = Double.POSITIVE_INFINITY;
    for (int i = 0; i < length; i++) {
      final double v = array[start + i];
      if (min > v) {
        maxIndex = i;
        min = v;
      }
    }
    return maxIndex;
  }

  @Override
  public double[] toArray() {
    final double[] copy = new double[dim()];
    System.arraycopy(this.array, start, copy, 0, length);
    return copy;
  }

  @Override
  public double get(int i) {
    return array[i + start];
  }

  @Override
  public Vec set(int i, double val) {
    array[start + i] = val;
    return this;
  }

  @Override
  public Vec adjust(int i, double increment) {
    array[start + i] += increment;
    return this;
  }

  @Override
  public VecIterator nonZeroes() {
    return new SkipVecNZIterator(this);
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
  public ArrayVec sub(int start, int length) {
    return new ArrayVec(array, this.start + start, length);
  }

  public void assign(ArrayVec vec) {
    ArrayTools.assign(array, start, vec.array, vec.start, length);
  }
}
