package com.spbsu.commons.math.vectors.impl.vectors;

import com.spbsu.commons.math.vectors.*;
import com.spbsu.commons.math.vectors.impl.iterators.SkipVecNZIterator;
import com.spbsu.commons.util.ArrayPart;
import com.spbsu.commons.util.ArrayTools;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:25:41
 */
public class ArrayVec extends Vec.Stub {
  public final ArrayPart<double[]> data;
  public ArrayVec(double... values) {
    data = new ArrayPart<double[]>(values);
  }

  public ArrayVec(int dim) {
    data = new ArrayPart<double[]>(new double[dim]);
  }

  public ArrayVec(double[] values, int offset, int length) {
    data = new ArrayPart<double[]>(values, offset, length);
  }

  @Override
  public int dim() {
    return data.length;
  }

  public void add(ArrayVec right) {
    ArrayTools.add(data.array, data.start, right.data.array, right.data.start, data.length);
  }

  public void fill(double v) {
    ArrayTools.fill(data.array, data.start, data.length, v);
  }

  public void scale(double s) {
    ArrayTools.mul(data.array, data.start, data.length, s);
  }

  public void scale(ArrayVec other) {
    ArrayTools.scale(data.array, data.start, other.data.array, other.data.start, data.length);
  }

  public double mul(ArrayVec other) {
    return ArrayTools.mul(data.array, data.start, other.data.array, other.data.start, data.length);
  }

  public double l2(ArrayVec other) {
    return ArrayTools.l2(data.array, data.start, other.data.array, other.data.start, data.length);
  }

  public int max() {
    int maxIndex = -1;
    double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < data.length; i++) {
      final double v = data.array[data.start + i];
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
    for (int i = 0; i < data.length; i++) {
      final double v = data.array[data.start + i];
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
    System.arraycopy(this.data.array, data.start, copy, 0, data.length);
    return copy;
  }

  @Override
  public double get(int i) {
    return data.array[i + data.start];
  }

  @Override
  public Vec set(int i, double val) {
    data.array[data.start + i] = val;
    return this;
  }

  @Override
  public Vec adjust(int i, double increment) {
    data.array[data.start + i] += increment;
    return this;
  }

  @Override
  public VecIterator nonZeroes() {
    return new SkipVecNZIterator(this);
  }

  @Override
  public ArrayVec sub(int start, int length) {
    return new ArrayVec(data.array, this.data.start + start, length);
  }

  public void assign(ArrayVec vec) {
    ArrayTools.assign(data.array, data.start, vec.data.array, vec.data.start, data.length);
  }
}
