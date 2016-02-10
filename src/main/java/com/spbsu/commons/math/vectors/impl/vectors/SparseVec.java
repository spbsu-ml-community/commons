package com.spbsu.commons.math.vectors.impl.vectors;

import com.spbsu.commons.math.vectors.impl.basis.IntBasis;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 15:52:45
 */
public class SparseVec extends CustomBasisVec<IntBasis> {
  public SparseVec(final int dim, final int[] indeces, final double[] values) {
    super(new IntBasis(dim));
    init(indeces, values);
  }

  public SparseVec(final int dim) {
    super(new IntBasis(dim));
  }

  public SparseVec(final int dim, int capacityHint) {
    super(new IntBasis(dim), capacityHint);
  }

  protected SparseVec() {
  }
}
