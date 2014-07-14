package com.spbsu.commons.math.vectors.impl.vectors;

import com.spbsu.commons.math.vectors.*;
import com.spbsu.commons.math.vectors.impl.basis.IntBasis;
import com.spbsu.commons.math.vectors.impl.iterators.SparseVecNZIterator;
import com.spbsu.commons.util.ArrayTools;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 15:52:45
 */
public class SparseVec extends CustomBasisVec<IntBasis> {
  public SparseVec(int dim, int[] indeces, double[] values) {
    super(new IntBasis(dim));
    init(indeces, values);
  }

  public SparseVec(int dim) {
    super(new IntBasis(dim));
  }

  protected SparseVec() {
  }
}
