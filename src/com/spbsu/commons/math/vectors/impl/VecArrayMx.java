package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.*;
import com.spbsu.commons.math.vectors.impl.idxtrans.SubMxTransformation;
import com.spbsu.commons.math.vectors.impl.idxtrans.SubVecTransformation;
import com.spbsu.commons.math.vectors.impl.iterators.MxIteratorImpl;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:25:41
 */
public class VecArrayMx implements Mx {
  public final Vec[] vec;
  private int columns;

  public VecArrayMx(Vec[] vec) {
    this.vec = vec;
    columns = vec[0].dim();
  }

  public double get(int i, int j) {
    return vec[i].get(j);
  }
  @Override
  public Mx set(int i, int j, double val) {
    vec[i].set(j, val);
    return this;
  }

  @Override
  public Mx adjust(int i, int j, double increment) {
    vec[i].adjust(j, increment);
    return this;
  }

  @Override
  public Mx sub(int i, int j, int height, int width) {
    Vec[] rows = new Vec[height];
    for (int r = 0; r < rows.length; r++) {
      rows[r] = vec[i].sub(j, width);
    }
    return new VecArrayMx(rows);
  }

  @Override
  public Vec row(int i) {
    return vec[i];
  }

  @Override
  public Vec col(int j) {
    return new IndexTransVec(this, new SubMxTransformation(columns, 0, j, 1, rows()));
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < rows(); i++) {
      for (int j = 0; j < columns(); j++) {
        builder.append(j > 0 ? "\t" : "");
        builder.append(get(i, j));
      }
      builder.append('\n');
    }
    return builder.toString();
  }

  @Override
  public double get(int i) {
    return vec[i / columns].get(i % columns);
  }

  @Override
  public Vec set(int i, double val) {
    vec[i / columns].set(i % columns, val);
    return this;
  }

  @Override
  public Vec adjust(int i, double increment) {
    vec[i / columns].adjust(i % columns, increment);
    return this;
  }

  @Override
  public MxIterator nonZeroes() {
    return new MxIterator() {
      int row = 0;
      VecIterator rowIter = vec[0].nonZeroes();
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
        while (row < vec.length && !rowIter.advance()) {
          row++;
          rowIter = vec[row].nonZeroes();
        }
        return row < vec.length;
      }

      @Override
      public boolean seek(int pos) {
        rowIter = vec[pos / columns] .nonZeroes();
        return rowIter.seek(pos % columns);
      }

      @Override
      public double setValue(double v) {
        return rowIter.setValue(v);
      }
    };
  }
 
  @Override
  public MxBasis basis() {
    return new MxBasisImpl(columns, rows());
  }

  @Override
  public int dim() {
    return vec.length * columns;
  }

  @Override
  public double[] toArray() {
    final double[] result = new double[dim()];
    for (int r = 0; r < vec.length; r++) {
      final VecIterator viter = vec[r].nonZeroes();
      while (viter.advance()) {
        result[viter.index() + r * columns] = viter.value();
      }
    }
    return result;
  }

  @Override
  public Vec sub(int start, int len) {
    return new IndexTransVec(this, new SubVecTransformation(start, len));
  }

  @Override
  public int columns() {
    return columns;
  }

  @Override
  public int rows() {
    return dim()/columns;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof VecArrayMx && (((VecArrayMx)o).columns == columns) && ((VecArrayMx)o).vec.equals(vec);
  }

  @Override
  public int hashCode() {
    return (vec.hashCode() << 1) + columns;
  }
}
