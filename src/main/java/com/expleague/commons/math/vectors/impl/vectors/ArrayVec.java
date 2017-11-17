package com.expleague.commons.math.vectors.impl.vectors;

import com.expleague.commons.math.vectors.OperableVec;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecIterator;
import com.expleague.commons.math.vectors.impl.iterators.SkipVecNZIterator;
import com.expleague.commons.util.ArrayPart;
import com.expleague.commons.util.ArrayTools;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:25:41
 */
public class ArrayVec extends Vec.Stub implements OperableVec<ArrayVec> {
  public final ArrayPart<double[]> data;
  public ArrayVec(final double... values) {
    data = new ArrayPart<>(values);
  }

  public ArrayVec(final int dim) {
    if (dim < 0)
      throw new NegativeArraySizeException();
    data = new ArrayPart<>(new double[dim]);
  }

  public ArrayVec(final double[] values, final int offset, final int length) {
    if (offset < 0 || offset + length > values.length)
      throw new ArrayIndexOutOfBoundsException();
//    for (int i = offset; i < offset + length; i++) {
//      if (Double.isNaN(values[i]) || Double.isInfinite(values[i]))
//        throw new IllegalArgumentException();
//    }
    data = new ArrayPart<>(values, offset, length);
  }

  @Override
  public int dim() {
    return data.length;
  }

  public void add(final ArrayVec right) {
    ArrayTools.add(data.array, data.start, right.data.array, right.data.start, data.length);
  }

  public void fill(final double v) {
//    if (Double.isNaN(v) || Double.isInfinite(v))
//      throw new IllegalArgumentException();
    ArrayTools.fill(data.array, data.start, data.length, v);
  }

  @Override
  public void inscale(ArrayVec other, double scale) {
//    if (Double.isNaN(scale) || Double.isInfinite(scale))
//      throw new IllegalArgumentException();
    ArrayTools.incscale(other.data.array, other.data.start, data.array, data.start, other.data.length, scale);
  }

  public void scale(final double s) {
//    if (Double.isNaN(s) || Double.isInfinite(s))
//      throw new IllegalArgumentException();
    ArrayTools.mul(data.array, data.start, data.length, s);
  }

  public void scale(final ArrayVec other) {
    ArrayTools.scale(data.array, data.start, other.data.array, other.data.start, data.length);
  }

  public double mul(final ArrayVec other) {
    return ArrayTools.mul(data.array, data.start, other.data.array, other.data.start, data.length);
  }

  public double l2(final ArrayVec other) {
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
  public void toArray(final double[] src, final int offset) {
    System.arraycopy(this.data.array, data.start, src, offset, data.length);
  }

  @Override
  public double get(final int i) {
    return data.array[i + data.start];
  }

  @Override
  public Vec set(final int i, final double val) {
//    if (Double.isNaN(val) || Double.isInfinite(val))
//      throw new IllegalArgumentException();
    data.array[data.start + i] = val;
    return this;
  }

  @Override
  public Vec adjust(final int i, final double increment) {
//    if (Double.isNaN(increment) || Double.isInfinite(increment))
//      throw new IllegalArgumentException();
    data.array[data.start + i] += increment;
    return this;
  }

  @Override
  public VecIterator nonZeroes() {
    return new SkipVecNZIterator(this);
  }

  @Override
  public ArrayVec sub(final int start, final int length) {
    return new ArrayVec(data.array, this.data.start + start, length);
  }

  public void assign(final ArrayVec vec) {
    ArrayTools.assign(data.array, data.start, vec.data.array, vec.data.start, data.length);
  }
}
