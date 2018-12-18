package com.expleague.commons.math.vectors.impl.iterators;

import com.expleague.commons.math.vectors.VecIterator;
import com.expleague.commons.math.vectors.impl.vectors.CustomBasisVec;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntDoubleProcedure;

/**
* User: solar
* Date: 10/9/12
* Time: 9:05 AM
*/
public class SparseVecNZIterator implements VecIterator {
  int index = -1;
  boolean needRemove = false;
  int size;
  int logSize;
  int idx = -1;
  private final TIntArrayList indices;
  private final TDoubleArrayList values;

  public SparseVecNZIterator(final CustomBasisVec sparseVec) {
    this.indices = sparseVec.indices;
    this.values = sparseVec.values;
    size = indices.size();
    logSize = (int)Math.log(size);
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
  public final boolean advance(int to, TIntDoubleProcedure todo) {
    if (needRemove) {
      needRemove = false;
      indices.removeAt(index);
      values.removeAt(index);
      size--;
    }
    int index = this.index;
    int idx = -1;
    while(index < size && (idx = indices.getQuick(index)) < to) {
      todo.execute(idx, values.getQuick(index));
      index++;
    }
    this.index = index;
    this.idx = idx;
    return index < size;
  }


  @Override
  public boolean seek(final int pos) {
    advance();
    index = indices.binarySearch(pos);
    return isValid();
  }

  @Override
  public double setValue(final double v) {
    values.setQuick(index, v);
    needRemove = (v == 0);
    return v;
  }

  @Override
  public boolean isValid() {
    return index >= 0 && index < size;
  }
}
