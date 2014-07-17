package com.spbsu.commons.math.vectors.impl.mx;

import com.spbsu.commons.math.vectors.Mx;
import com.spbsu.commons.math.vectors.MxIterator;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.impl.idxtrans.SubMxTransformation;
import com.spbsu.commons.math.vectors.impl.vectors.IndexTransVec;
import com.spbsu.commons.seq.ArraySeq;
import com.spbsu.commons.seq.Seq;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:25:41
 */
public class RowsVecArrayMx extends Mx.Stub {
  public final Seq<Vec> vec;
  private int columns;

  public RowsVecArrayMx(Vec[] vec) {
    this.vec = new ArraySeq<>(vec);
    columns = vec[0].dim();
  }

  public RowsVecArrayMx(Seq<Vec> vec) {
    this.vec = vec;
    columns = vec.at(0).dim();
  }

  public double get(int i, int j) {
    return vec.at(i).get(j);
  }
  @Override
  public Mx set(int i, int j, double val) {
    vec.at(i).set(j, val);
    return this;
  }

  @Override
  public Mx adjust(int i, int j, double increment) {
    vec.at(i).adjust(j, increment);
    return this;
  }

  @Override
  public Mx sub(int i, int j, int height, int width) {
    final Vec[] rows = new Vec[height];
    for (int r = 0; r < rows.length; r++) {
      rows[r] = vec.at(i + r).sub(j, width);
    }
    return new RowsVecArrayMx(rows);
  }

  @Override
  public Vec row(int i) {
    return vec.at(i);
  }

  @Override
  public Vec col(int j) {
    return new IndexTransVec(this, new SubMxTransformation(columns, 0, j, rows(), 1));
  }

  @Override
  public double get(int i) {
    return vec.at(i / columns).get(i % columns);
  }

  @Override
  public Vec set(int i, double val) {
    vec.at(i / columns).set(i % columns, val);
    return this;
  }

  @Override
  public Vec adjust(int i, double increment) {
    vec.at(i / columns).adjust(i % columns, increment);
    return this;
  }

  @Override
  public MxIterator nonZeroes() {
    return new MxIterator() {
      int row = 0;
      VecIterator rowIter = vec.at(0).nonZeroes();
      @Override
      public int column() {
        return rowIter.index();
      }

      @Override
      public int row() {
        return row;
      }

      @Override
      public int index() {
        return row * columns + rowIter.index();
      }

      @Override
      public double value() {
        return rowIter.value();
      }

      @Override
      public boolean isValid() {
        return rowIter.isValid();
      }

      @Override
      public boolean advance() {
        while (row < vec.length() && !rowIter.advance()) {
          row++;
          rowIter = vec.at(row).nonZeroes();
        }
        return row < vec.length();
      }

      @Override
      public boolean seek(int pos) {
        rowIter = vec.at(pos / columns).nonZeroes();
        return rowIter.seek(pos % columns);
      }

      @Override
      public double setValue(double v) {
        return rowIter.setValue(v);
      }
    };
  }

  @Override
  public double[] toArray() {
    final double[] result = new double[dim()];
    for (int r = 0; r < vec.length(); r++) {
      final VecIterator viter = vec.at(r).nonZeroes();
      while (viter.advance()) {
        result[viter.index() + r * columns] = viter.value();
      }
    }
    return result;
  }

  @Override
  public boolean isImmutable() {
    return false;
  }

  @Override
  public int columns() {
    return columns;
  }

  @Override
  public int rows() {
    return vec.length();
  }
}
