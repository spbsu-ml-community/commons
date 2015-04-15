package com.spbsu.commons.math.vectors.impl.mx;

import org.jetbrains.annotations.NotNull;

import com.spbsu.commons.math.vectors.MxBasis;
import com.spbsu.commons.math.vectors.MxIterator;
import com.spbsu.commons.math.vectors.impl.basis.MxBasisImpl;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.math.vectors.Mx;
import com.spbsu.commons.math.vectors.Vec;

/**
 * jmll
 * ksen
 * 28.February.2015 at 23:28
 */
public class ColMajorArrayMx extends Mx.Stub {

  /*
  *  | 1 4 7 10 |
  *  | 2 5 8 11 | = Mx ~ data = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
  *  | 3 6 9 12 |
  *
  * */
  private double[] data;
  private int rows;

  public ColMajorArrayMx(final int rows, final int columns) {
    data = new double[rows * columns];
    this.rows = rows;
  }

  public ColMajorArrayMx(final int rows, final double[] data) {
    this.data = data;
    this.rows = rows;
  }

  @Override
  public double get(final int i, final int j) {
    return data[i + j * rows];
  }

  @Override
  public Mx set(final int i, final int j, final double value) {
    data[i + j * rows] = value;
    return this;
  }

  @Override
  public Mx adjust(final int i, final int j, final double increment) {
    data[i + j * rows] += increment;
    return this;
  }

  @Override
  public Mx sub(final int i, final int j, final int height, final int width) {
    final double[] subData = new double[height * width];
    for (int column = j; column < j + width; column++) {
      System.arraycopy(data, i + column * rows, subData, (column - j) * height, height);
    }
    return new ColMajorArrayMx(height, subData);
  }

  @Override
  public int columns() {
    return data.length / rows;
  }

  @Override
  public int rows() {
    return rows;
  }

  @NotNull
  @Override
  public double[] toArray() {
    return data;
  }

  @Override
  public double get(final int i) {
    return data[i];
  }

  @Override
  public Vec set(final int i, final double value) {
    data[i] = value;
    return this;
  }

  @Override
  public Vec adjust(final int i, final double increment) {
    data[i] += increment;
    return this;
  }

  @Override
  public Vec row(final int i) {
    final double[] row = new double[columns()];
    for (int j = 0; j < row.length; j++) {
      row[j] = get(i, j);
    }
    return new ArrayVec(row);
  }

  @Override
  public Vec col(final int j) {
    final double[] column = new double[rows];
    System.arraycopy(data, j * rows, column, 0, rows);
    return new ArrayVec(column);
  }

  @Override
  public MxBasis basis() {
    return new MxBasisImpl(rows, columns());
  }

  @Override
  public boolean isImmutable() {
    return false;
  }

  @Override
  public Class<Double> elementType() {
    return double.class;
  }

  @Override
  public MxIterator nonZeroes() {
    return new MxIterator() {
      private int pointer = -rows;
      private int counter = -1;

      @Override
      public int column() {
        return pointer / rows;
      }

      @Override
      public int row() {
        return pointer % rows;
      }

      @Override
      public int index() {
        return pointer;
      }

      @Override
      public double value() {
        return data[index()];
      }

      @Override
      public boolean isValid() {
        return -1 < counter && counter < dim();
      }

      @Override
      public boolean advance() {
        do {
          if ((pointer += rows) > dim() - 1) {
            pointer++;
            pointer %= dim();
          }
          counter++;
        } while (counter < dim() && data[pointer] == 0);
        return isValid();
      }

      @Override
      public boolean seek(final int position) {
        pointer = position;
        return isValid();
      }

      @Override
      public double setValue(final double value) {
        data[index()] = value;
        return value;
      }
    };
  }
}
