package com.spbsu.commons.math.vectors;

/**
* User: solar
* Date: 01.08.12
* Time: 15:51
*/
class MxBasisImpl implements MxBasis {
  private final int columns;
  private final int rows;
  private final int size;
  public MxBasisImpl(int columns, int rows) {
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
  public boolean equals(Object o) {
    return o instanceof MxBasis && (((MxBasis)o).columns() == columns()) && (((MxBasis)o).rows() == rows());
  }
}
