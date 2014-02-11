package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.*;
import com.spbsu.commons.math.vectors.impl.iterators.SparseVecNZIterator;
import com.spbsu.commons.util.ArrayTools;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 15:52:45
 */
public class SparseVec<B extends Basis> implements Vec {
  public TIntArrayList indices = new TIntArrayList();
  public TDoubleArrayList values = new TDoubleArrayList();
  protected B basis;

  public SparseVec(B basis, int[] indeces, double[] values) {
    this.basis = basis;
    init(indeces, values);
  }

  public SparseVec(B basis) {
    this.basis = basis;
  }

  protected SparseVec() {
  }

  protected void init(int[] indeces, double[] values) {
    ArrayTools.parallelSort(indeces, values);
    this.indices.add(indeces);
    this.values.add(values);
  }

  @Override
  public double get(int i) {
    final int realIndex = indices.binarySearch(i);
    if (realIndex >= 0 && indices.getQuick(realIndex) == i) {
      return values.getQuick(realIndex);
    }
    return 0;
  }

  @Override
  public Vec set(int i, double val) {
    final int realIndex = indices.binarySearch(i);
    if (realIndex >= 0 && indices.getQuick(realIndex) == i) {
      if (val == 0) {
        values.remove(realIndex);
        indices.remove(realIndex);
      }
      else
        values.setQuick(realIndex, val);
    }
    else if (val != 0) {
      indices.insert(-realIndex - 1, i);
      values.insert(-realIndex - 1, val);
    }

    return this;
  }

  @Override
  public Vec adjust(int i, double increment) {
    final int realIndex = indices.binarySearch(i);
    if (realIndex >= 0 && indices.getQuick(realIndex) == i) {
      final double newValue = values.getQuick(realIndex) + increment;
      if (newValue == 0) {
        values.remove(realIndex);
        indices.remove(realIndex);
      }
      else values.setQuick(realIndex, newValue);
    }
    else if (increment != 0) {
      indices.insert(-realIndex - 1, i);
      values.insert(-realIndex - 1, increment);
    }

    return this;
  }

  @Override
  public VecIterator nonZeroes() {
    return new SparseVecNZIterator(this);
  }
  
  public B basis() {
    return basis;
  }

  @Override
  public int dim() {
    return basis.size();
  }

  @Override
  public synchronized double[] toArray() {
    double[] result = new double[basis.size()];
    VecIterator iter = nonZeroes();
    while (iter.advance())
      result[iter.index()] = iter.value();
    return result;
  }

  @Override
  public Vec sub(int start, int len) {
    int end = start + len;
    int sindex = 0;
    int eindex = 0;
    for (int i = 0; i < indices.size() && indices.get(i) < end; i++) {
      if (indices.get(i) < start)
        sindex++;
      eindex++;
    }
    int[] indices = new int[eindex - sindex];
    double[] values = new double[eindex - sindex];
    for (int i = 0; i < indices.length; i++) {
      indices[i] = this.indices.get(i + sindex) - start;
      values[i] = this.values.get(i + sindex);
    }
    return new SparseVec<IntBasis>(new IntBasis(len), indices, values);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Vec && VecTools.equals(this, (Vec) o);
  }

  @Override
  public int hashCode() {
    return VecTools.hashCode(this);
  }

  /**way faster than set, but index must be greater all indices we already have in vector */
  public void add(int index, double v) {
    if (v != 0.) {
      this.indices.add(index);
      this.values.add(v);
    }
  }
}
