package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.Basis;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.VecTools;
import com.spbsu.commons.math.vectors.impl.iterators.SparseVecNZIterator;
import com.spbsu.commons.util.ArrayTools;
import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;

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
  
  @Override
  public B basis() {
    return basis;
  }

  @Override
  public int dim() {
    return basis.size();
  }

  @Override
  public double[] toArray() {
    double[] result = new double[basis.size()];
    VecIterator iter = nonZeroes();
    while (iter.advance())
      result[iter.index()] = iter.value();
    return result;
  }

  @Override
  public boolean sparse() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Vec && VecTools.equals(this, (Vec) o);
  }

  @Override
  public int hashCode() {
    return VecTools.hashCode(this);
  }

}
