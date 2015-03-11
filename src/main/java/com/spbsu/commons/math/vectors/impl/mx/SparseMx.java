package com.spbsu.commons.math.vectors.impl.mx;

import com.spbsu.commons.math.vectors.impl.basis.MxBasisImpl;
import com.spbsu.commons.math.vectors.impl.vectors.SparseVec;

/**
 * Created by vkokarev on 08.04.14.
 * Something like Compressed Row Storage (CRS)
 * This structure is for efficient storing sparse matrix
 * Use it, if you have most of rows and only few columns
 * For matrix with a great number of empty rows it is better to implement CCS
 */
public class SparseMx extends CustomBasisMx<MxBasisImpl> {
  public SparseMx(final int rows, final int columns) {
    super(new MxBasisImpl(rows, columns));
  }

  public SparseMx(final SparseVec[] rows) {
    super(new MxBasisImpl(rows.length, rows[0].dim()), rows);
  }
}
