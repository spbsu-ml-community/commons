package com.spbsu.commons.math.vectors;

import com.spbsu.commons.math.vectors.impl.SparseVec;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by vkokarev on 08.04.14.
 * Something like Compressed Row Storage (CRS)
 * This structure is for efficient storing sparse matrix
 * Use it, if you have most of rows and only few columns
 * For matrix with a great number of empty rows it is better to implement CCS
 */
public class SparseMx<B extends MxBasis> implements Mx {
  private B mxBasis;
  private final Vec emptyRow;
  private IntBasis vecBasis;
  private SparseVec<IntBasis> rows[];

  public SparseMx(final B mxBasis) {
    this.mxBasis = mxBasis;
    this.vecBasis = new IntBasis(mxBasis.columns());
    this.rows = new SparseVec[mxBasis.rows()];
    this.emptyRow = new SparseVec<IntBasis>(this.vecBasis);
  }

  public SparseMx(final B mxBasis, final SparseVec<IntBasis>[] rows) {
    this.mxBasis = mxBasis;
    this.rows = rows;
    this.vecBasis = new IntBasis(mxBasis.columns());
    this.emptyRow = new SparseVec<IntBasis>(vecBasis);
  }

  @Override
  public double get(final int i, final int j) {
    rangeCheck(i, j);
    final SparseVec<IntBasis> row = rows[i];
    if (row != null) {
      return row.get(j);
    }
    return .0;
  }

  @Override
  public Mx set(final int i, final int j, final double val) {
    rangeCheck(i, j);
    SparseVec<IntBasis> row = rows[i];
    if (row != null) {
      row.set(j, val);
    } else {
      row = new SparseVec<IntBasis>(vecBasis);
      row.set(j, val);
      rows[i] = row;
    }
    return this;
  }


  /**
   * nothing is checked, use on your own responsibility
   */
  public Mx setRow(final int i, final SparseVec<IntBasis> row) {
    rows[i] = row;
    return this;
  }

  @Override
  public Mx adjust(final int i, final int j, final double increment) {
    rangeCheck(i, j);

    if (increment != 0) {
      if (rows[i] == null) {
        rows[i] = new SparseVec<IntBasis>(vecBasis);
      }
      rows[i].adjust(j, increment);
    }
    return this;
  }

  @Override
  public Mx sub(final int i, final int j, final int height, final int width) {
    final SparseVec<IntBasis>[] copyRows = new SparseVec[height];
    for (int k = 0; k < height; ++k) {
      if (rows[k + i] != null)
        copyRows[k] = (SparseVec<IntBasis>) rows[k + i].sub(j, width);
    }
    return new SparseMx<MxBasisImpl>(new MxBasisImpl(height, width), copyRows);
  }

  /**
   * @important NEVER CHANGE RETURNED VALUE, IT HAS SIDE AFFECTS
   */
  @Override
  public Vec row(final int i) {
    final Vec row = rows[i];
    return row != null ? row : emptyRow;
  }

  /**
   * O(m), if you need this operation, it is better, to implements CCS
   *
   * @param j
   * @return
   */
  @Override
  public Vec col(final int j) {
    final SparseVec<IntBasis> result = new SparseVec<IntBasis>(new IntBasis(mxBasis.columns()));
    for (int i = 0; i < rows.length; ++i) {
      if (rows[i] != null) {
        final double val = rows[i].get(j);
        if (val != 0) {
          result.add(i, val);
        }
      }
    }
    return result;
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
    return null;
  }

  @Override
  public MxIterator nonZeroes() {
    return new SparseMxIterator();
  }

  @Override
  public int dim() {
    return mxBasis.columns() * mxBasis.rows();
  }

  @Override
  public double[] toArray() {
    return new double[0];
  }

  @Override
  public Vec sub(final int start, final int len) {
    throw new NotImplementedException();
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
      if (cRow >= 0) {
        cIter = rows[cRow].nonZeroes();
      } else {
        cIter = emptyRow.nonZeroes();
      }
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
      throw new NotImplementedException();
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
