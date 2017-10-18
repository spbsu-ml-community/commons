package com.expleague.commons.math.vectors.impl.iterators;

import com.expleague.commons.math.vectors.VecIterator;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
* User: solar
* Date: 10/10/12
* Time: 8:58 PM
*/
public class TransformedSparseVecIterator implements VecIterator {
  int index;
  int size;
  boolean needRemove = false;
  private final TIntArrayList origIndices;
  private final TDoubleArrayList origValues;
  private final TIntArrayList nzIndices;
  private final TIntArrayList nzTransformed;

  public TransformedSparseVecIterator(final TIntArrayList origIndices, final TDoubleArrayList origValues, final TIntArrayList nzIndices, final TIntArrayList nzTransformed) {
    this.origIndices = origIndices;
    this.origValues = origValues;
    this.nzIndices = nzIndices;
    this.nzTransformed = nzTransformed;
    index = -1;
    size = nzIndices.size();
  }

  @Override
  public int index() {
    return nzIndices.getQuick(index);
  }

  @Override
  public double value() {
    return origValues.getQuick(nzTransformed.getQuick(index));
  }

  @Override
  public final boolean advance() {
    if (needRemove) {
      needRemove = false;
      final int oldIndex = nzTransformed.getQuick(index);
      origIndices.removeAt(oldIndex);
      origValues.removeAt(oldIndex);
      nzTransformed.removeAt(index);
      nzIndices.removeAt(index);
      size--;
      for (int i = 0; i < size; i++) {
        final int old = nzTransformed.getQuick(i);
        if (old > oldIndex) {
          nzTransformed.setQuick(i, old - 1);
        }
      }
    }
    else index++;
    return index < size;
  }

  @Override
  public boolean seek(final int pos) {
    advance();
    if (pos == 0) {
      index = -1;
      return false;
    }
    index = nzIndices.binarySearch(pos - 1);
    if (index < -1)
      index = -index - 2;
    return isValid();
  }

  @Override
  public double setValue(final double v) {
    origValues.setQuick(nzTransformed.getQuick(index), v);
    needRemove = (v == 0);
    return v;
  }

  @Override
  public boolean isValid() {
    return index >= 0 && index < size;
  }
}
