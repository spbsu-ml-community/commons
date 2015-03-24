package com.spbsu.commons.math.vectors.impl.vectors;

import java.util.Arrays;


import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;

/**
 * Created by vkokarev on 10.12.14.
 */
public class ConcatVec extends Vec.Stub {
  protected final Vec[] origin;
  protected final int[] offsets;
  protected int dim;
  public ConcatVec(final Vec ... origin) {
    this.origin = origin;
    this.offsets = new int[origin.length];
    for (int i = 1; i < origin.length; i++) {
      offsets[i] = offsets[i - 1] + origin[i - 1].dim();
    }
    dim = offsets[offsets.length - 1] + origin[origin.length - 1].dim();
  }

  @Override
  public double get(final int i) {
    int originIndex = Arrays.binarySearch(offsets, i);
    originIndex = originIndex >= 0 ? originIndex : -originIndex - 2;
    return origin[originIndex].get(i - offsets[originIndex]);
  }

  @Override
  public Vec set(final int i, final double val) {
    throw new UnsupportedOperationException("you are not allowed to change this vec");
  }

  @Override
  public Vec adjust(final int i, final double increment) {
    throw new UnsupportedOperationException("you are not allowed to change this vec");
  }

  @Override
  public void toArray(final double[] array, final int originOffset) {
    for (int i = 0; i < origin.length; ++i) {
      origin[i].toArray(array, offsets[i]);
    }
  }

  @Override
  public double[] toArray() {
    final double[] data = new double[dim()];
    toArray(data, 0);
    return data;
  }

  @Override
  public VecIterator nonZeroes() {
    return new VecIterator() {
      int originIdx = 0;
      VecIterator citer = origin[0].nonZeroes();
      @Override
      public int index() {
        return offsets[originIdx] + citer.index();
      }

      @Override
      public double value() {
        return citer.value();
      }

      @Override
      public boolean isValid() {
        return citer.isValid();
      }

      @Override
      public boolean advance() {
        if (citer.advance()) {
          return true;
        }
        while (++originIdx < origin.length) {
          citer = origin[originIdx].nonZeroes();
          if (citer.advance())
            return true;
        }
        return false;
      }

      @Override
      public boolean seek(final int pos) {
        for (int i = 0; i < pos; ++i) {
          if (!advance())
            return false;
        }
        return true;
      }

      @Override
      public double setValue(final double v) {
        throw new UnsupportedOperationException("you are not allowed to change this vec");
      }
    };
  }



  @Override
  public int dim() {
    return dim;
  }

  @Override
  public Vec sub(final int start, final int end) {
    return null;
  }
}
