package com.expleague.commons.math.vectors.impl.mx;

import com.expleague.commons.math.vectors.*;
import com.expleague.commons.math.vectors.impl.basis.IntBasis;
import com.expleague.commons.math.vectors.impl.basis.MxBasisImpl;
import com.expleague.commons.math.vectors.impl.vectors.SparseVec;

/**
 * Created by vkokarev on 08.04.14.
 * Something like Compressed Row Storage (CRS)
 * This structure is for efficient storing sparse matrix
 * Use it, if you have most of rows and only few columns
 * For matrix with a great number of empty rows it is better to implement CCS
 */
public class CustomBasisMx<B extends MxBasis> extends Mx.Stub {
  private final B mxBasis;
  private final IntBasis vecBasis;
  private final SparseVec[] rows;

  public CustomBasisMx(final B mxBasis) {
    this.mxBasis = mxBasis;
    this.vecBasis = new IntBasis(mxBasis.columns());
    this.rows = new SparseVec[mxBasis.rows()];
  }

  public CustomBasisMx(final B mxBasis, final SparseVec[] rows) {
    this.mxBasis = mxBasis;
    this.rows = rows;
    this.vecBasis = new IntBasis(mxBasis.columns());
  }

  @Override
  public double get(final int i, final int j) {
    rangeCheck(i, j);
    final SparseVec row = rows[i];
    if (row != null) {
      return row.get(j);
    }
    return .0;
  }

  @Override
  public Mx set(final int i, final int j, final double val) {
    rangeCheck(i, j);
    SparseVec row = rows[i];
    if (row != null) {
      row.set(j, val);
    } else {
      row = new SparseVec(vecBasis.size());
      row.set(j, val);
      rows[i] = row;
    }
    return this;
  }


  /**
   * nothing is checked, use on your own responsibility
   */
  public Mx setRow(final int i, final SparseVec row) {
    rows[i] = row;
    return this;
  }

  @Override
  public Mx adjust(final int i, final int j, final double increment) {
    rangeCheck(i, j);

    if (increment != 0) {
      if (rows[i] == null) {
        rows[i] = new SparseVec(vecBasis.size());
      }
      rows[i].adjust(j, increment);
    }
    return this;
  }

  @Override
  public Mx sub(final int i, final int j, final int height, final int width) {
    final SparseVec[] copyRows = new SparseVec[height];
    for (int k = 0; k < height; ++k) {
      if (rows[k + i] != null)
        copyRows[k] = (SparseVec) rows[k + i].sub(j, width);
    }
    return new CustomBasisMx<>(new MxBasisImpl(height, width), copyRows);
  }

  /**
   * @important NEVER CHANGE RETURNED VALUE, IT HAS SIDE AFFECTS
   */
  @Override
  public SparseVec row(final int i) {
    final SparseVec row = rows[i];
    return row != null ? row : (rows[i] = new SparseVec(columns()));
  }

  @Override
  public double get(final int i) {
    return get(i / mxBasis.columns(), i % mxBasis.columns());
  }

  @Override
  public Vec set(final int i, final double val) {
    return set(i / mxBasis.columns(), i % mxBasis.columns(), val);
  }

  @Override
  public Vec adjust(final int i, final double increment) {
    return adjust(i / mxBasis.columns(), i % mxBasis.columns(), increment);
  }

  @Override
  public MxIterator nonZeroes() {
    return new SparseMxIterator();
  }

  @Override
  public double[] toArray() {
    return new double[0];
  }

  @Override
  public boolean isImmutable() {
    return false;
  }

  @Override
  public MxBasis basis() {
    return mxBasis;
  }

  @Override
  public int columns() {
    return mxBasis.columns();
  }

  @Override
  public int rows() {
    return mxBasis.rows();
  }

  public void clear() {
    for (SparseVec row : rows) {
      if (row != null)
        row.clear();
    }
  }

  public boolean isRowEmpty(int i) {
    return rows[i] == null;
  }

  private void rangeCheck(final int i, final int j) {
    if (i < 0 || i >= mxBasis.rows() || j < 0 || j >= mxBasis.columns())
      throw new IndexOutOfBoundsException("bad index [" + i + "," + j + "]");

  }

  private class SparseMxIterator implements MxIterator {
    private VecIterator cIter;
    private int cRow;
    private boolean wasRemoved;

    SparseMxIterator() {
      cRow = nextNotNullRow(0);
      if (cRow >= 0)
        cIter = rows[cRow].nonZeroes();
      else
        cIter = null;
    }

    @Override
    public int column() {
      return cIter.index();
    }

    @Override
    public int row() {
      return cRow;
    }

    @Override
    public int index() {
      return cRow * mxBasis.columns() + cIter.index();
    }

    @Override
    public double value() {
      return cIter.value();
    }

    @Override
    public boolean isValid() {
      return cRow >= 0 && cRow < mxBasis.rows() && cIter.isValid();
    }

    @Override
    public boolean advance() {
      if (cIter == null)
        return false;
      if (!cIter.advance()) {
        //check if we have removed row
        if (wasRemoved && !rows[cRow].nonZeroes().isValid())
          rows[cRow] = null;

        //move iterator
        cIter = null;
        cRow = nextNotNullRow(cRow + 1);
        if (cRow >= 0 && cRow < mxBasis.rows()) {
          cIter = rows[cRow].nonZeroes();
          cIter.advance();
        }
      }
      wasRemoved = false;
      return cIter != null;
    }

    @Override
    public boolean seek(final int pos) {
      throw new UnsupportedOperationException();
    }

    @Override
    public double setValue(final double v) {
      wasRemoved = (v == 0.);
      return cIter.setValue(v);
    }

    private int nextNotNullRow(final int start) {
      for (int i = start; i < rows.length; ++i) {
        if (rows[i] != null) {
          return i;
        }
      }
      return -1;
    }
  }
}
