package com.expleague.commons.math.vectors.impl.basis;

import com.expleague.commons.math.vectors.MxBasis;

/**
* User: solar
* Date: 01.08.12
* Time: 15:51
*/
public class MxBasisImpl implements MxBasis {
  private final int columns;
  private final int rows;
  private final int size;
  public MxBasisImpl(final int rows, final int columns) {
    this.columns = columns;
    this.rows = rows;
    size = columns * rows;
  }

  @Override
  public int columns() {
    return columns;
  }

  @Override
  public int rows() {
    return rows;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public int hashCode() {
    return rows() + (columns << 5);
  }

  @Override
  public boolean equals(final Object o) {
    return o instanceof MxBasis && (((MxBasis)o).columns() == columns()) && (((MxBasis)o).rows() == rows());
  }
}
