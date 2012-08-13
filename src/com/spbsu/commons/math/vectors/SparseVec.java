package com.spbsu.commons.math.vectors;

import com.spbsu.commons.util.ArrayTools;
import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 15:52:45
 */
public class SparseVec<B extends Basis> implements Vec {
  TIntArrayList indexTransform = new TIntArrayList();
  TDoubleArrayList values = new TDoubleArrayList();
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
    this.indexTransform.add(indeces);
    this.values.add(values);
  }

  @Override
  public double get(int i) {
    final int realIndex = indexTransform.binarySearch(i);
    if (realIndex >= 0 && indexTransform.getQuick(realIndex) == i) {
      return values.getQuick(realIndex);
    }
    return 0;
  }

  @Override
  public Vec set(int i, double val) {
    final int realIndex = indexTransform.binarySearch(i);
    if (realIndex >= 0 && indexTransform.getQuick(realIndex) == i) {
      if (val == 0) {
        values.remove(realIndex);
        indexTransform.remove(realIndex);
      }
      else
        values.setQuick(realIndex, val);
    }
    else if (val != 0) {
      indexTransform.insert(-realIndex - 1, i);
      values.insert(-realIndex - 1, val);
    }

    return this;
  }

  @Override
  public Vec adjust(int i, double increment) {
    final int realIndex = indexTransform.binarySearch(i);
    if (realIndex >= 0 && indexTransform.getQuick(realIndex) == i) {
      final double newValue = values.getQuick(realIndex) + increment;
      if (newValue == 0) {
        values.remove(realIndex);
        indexTransform.remove(realIndex);
      }
      else values.setQuick(realIndex, newValue);
    }
    else if (increment != 0) {
      indexTransform.insert(-realIndex - 1, i);
      values.insert(-realIndex - 1, increment);
    }

    return this;
  }

  @Override
  public VecIterator nonZeroes() {
    return new VecIterator() {
      int index = -1;
      boolean needRemove = false;
      int size = indexTransform.size();
      int idx = -1;
      @Override
      public int index() {
        return idx >= 0 ? idx : (idx = indexTransform.getQuick(index));
      }

      @Override
      public double value() {
        return values.getQuick(index);
      }

      @Override
      public final boolean advance() {
        int index;
        if (needRemove) {
          needRemove = false;
          indexTransform.remove(index = this.index);
          values.remove(index);
          size--;
        }
        else index = ++this.index;
        idx = -1;
        return index < size;
      }

      @Override
      public double setValue(double v) {
        values.setQuick(index, v);
        needRemove = (v == 0);
        return v;
      }

      @Override
      public boolean isValid() {
        return index >= 0 && index < size;
      }
    };
  }
  
  @Override
  public int nonZeroesCount() {
    return indexTransform.size();
  }

  @Override
  public final B basis() {
    return basis;
  }

  @Override
  public int dim() {
    return basis.size();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Vec && VecTools.equals(this, (Vec)o);
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    final VecIterator iter = nonZeroes();
    while (iter.advance()) {
      hashCode <<= 1;
      hashCode += iter.index();
      hashCode += iter.value() * 10000;
    }
    return hashCode;
  }
}
