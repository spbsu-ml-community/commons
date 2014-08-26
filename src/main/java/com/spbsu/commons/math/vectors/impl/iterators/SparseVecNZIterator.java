package com.spbsu.commons.math.vectors.impl.iterators;

import com.spbsu.commons.math.vectors.impl.vectors.CustomBasisVec;
import com.spbsu.commons.math.vectors.VecIterator;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
* User: solar
* Date: 10/9/12
* Time: 9:05 AM
*/
public class SparseVecNZIterator implements VecIterator {
  int index = -1;
  boolean needRemove = false;
  int size;
  int idx = -1;
  private TIntArrayList indices;
  private TDoubleArrayList values;

  public SparseVecNZIterator(CustomBasisVec sparseVec) {
    this.indices = sparseVec.indices;
    this.values = sparseVec.values;
    size = indices.size();
  }

  @Override
  public int index() {
    return idx >= 0 ? idx : (idx = indices.getQuick(index));
  }

  @Override
  public double value() {
    return values.getQuick(index);
  }

  @Override
  public final boolean advance() {
    if (needRemove) {
      needRemove = false;
      indices.removeAt(index);
      values.removeAt(index);
      size--;
    }
    else index++;
    idx = -1;
    return index < size;
  }

  @Override
  public boolean seek(int pos) {
    advance();
    index = indices.binarySearch(pos - 1);
    return isValid();
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
}
