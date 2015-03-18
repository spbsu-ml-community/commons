package com.spbsu.commons.math.vectors.impl.vectors;

import com.spbsu.commons.math.vectors.Vec;

/**
 * Created by vkokarev on 13.03.15.
 */
public class FastAccessConcatVec extends ConcatVec {
  final int[] index;
  public FastAccessConcatVec(final Vec ... origin) {
    super(origin);
    index = new int[dim];
    int cOffset = 0;
    for (int i = 0; i < origin.length; i++) {
      final Vec vec = origin[i];
      final int vecLen = vec.dim();
      for (int j = 0; j < vecLen; ++j) {
        index[cOffset++] = i;
      }
    }
  }

  @Override
  public double get(final int i) {
    final int originIndex = index[i];
    return origin[originIndex].get(i - offsets[originIndex]);
  }
}
