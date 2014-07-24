package com.spbsu.commons.math.vectors.impl.mx;

import com.spbsu.commons.math.vectors.Mx;
import com.spbsu.commons.math.vectors.MxIterator;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.impl.idxtrans.SubMxTransformation;
import com.spbsu.commons.math.vectors.impl.vectors.IndexTransVec;
import com.spbsu.commons.seq.ArraySeq;
import com.spbsu.commons.seq.Seq;
import com.spbsu.commons.seq.VecSeq;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by vkokarev on 23.07.14.
 */
public class ColsVecSeqMx extends Mx.Stub {
  public final Seq<VecSeq> vec;
  private final int[] indices;
  private final int columns;
  private final int rows;

  public ColsVecSeqMx(VecSeq[] vecSeqs) {
    if (vecSeqs.length == 0)
      throw new IllegalArgumentException("Unable to create ColsVecArrayMx from empty array!");

    this.indices = new int[vecSeqs.length];

    int totalColumns = 0;
    for (int j = 0; j < vecSeqs.length; ++j) {
      final VecSeq vecSeq = vecSeqs[j];
      final int len = vecSeq.at(0).length();
      for (int i = 0; i < vecSeq.length(); ++i) {
        if (vecSeq.at(i).length() != len) {
          throw new IllegalArgumentException("different dimension of column block!");
        }
      }
      indices[j] = totalColumns;
      totalColumns += vecSeq.length();
    }
    columns = totalColumns;

    this.vec = new ArraySeq<VecSeq>(vecSeqs);

    this.rows = this.vec.at(0).at(0).length();
  }

  public double get(int i, int j) {
    final int idx = getIdx(j);
    return vec.at(idx).at(j - indices[idx]).get(i);
  }
  @Override
  public Mx set(int i, int j, double val) {
    final int idx = getIdx(j);
    vec.at(idx).at(i).set(j - indices[idx], val);
    return this;
  }

  @Override
  public Mx adjust(int i, int j, double increment) {
    final int idx = getIdx(j);
    vec.at(idx).at(i).adjust(j - indices[idx], increment);
    return this;
  }

  @Override
  public Mx sub(int i, int j, int height, int width) {
    throw new NotImplementedException();
  }


  @Override
  public Vec row(final int i) {
    return new IndexTransVec(this, new SubMxTransformation(columns(), i, 0, 1, columns())) {
      private int col = -1;
      @Override
      public VecIterator nonZeroes() {
        return new VecIterator() {
          @Override
          public int index() {
            return col;
          }

          @Override
          public double value() {
            final int idx = getIdx(col);
            return vec.at(idx).at(col - indices[idx]).get(i);
          }

          @Override
          public boolean isValid() {
            return col >= 0 && col < columns();
          }

          @Override
          public boolean advance() {
            ++col;
            while (isValid() && value() == 0)
              ++col;
            return isValid();
          }

          @Override
          public boolean seek(int pos) {
            col = pos;
            return isValid();
          }

          @Override
          public double setValue(double v) {
            final int idx = getIdx(col);
            return vec.at(idx).at(col - indices[idx]).set(i, v).get(i);
          }
        };
      }
    };
  }

//  @Override
//  public Vec row(int i) {
//    double vals[] = new double[columns()];
//    int idx = 0;
//    int zero = 0;
//    for (int j = 0; j < vec.length(); ++j) {
//      final VecSeq vs = vec.at(j);
//      for (int k = 0; k < vs.length(); ++k) {
//        vals[idx] = vs.at(k).get(i);
//        if (vals[idx++] == 0) {
//          zero++;
//        }
//      }
//    }
//
//    if (zero * 1. / columns() > .9)
//      return VecTools.copySparse(new ArrayVec(vals));
//    else
//      return new ArrayVec(vals);
//  }

  @Override
  public Vec col(int j) {
    final int idx = getIdx(j);
    final VecSeq columnSeq = vec.at(idx);
    final int shift = j - indices[idx];
    return columnSeq.at(shift);
  }

  @Override
  public MxIterator nonZeroes() {
    return new ColsVecMxIterator();
  }

  @Override
  public double get(int i) {
    return get(i / columns, i % columns);
  }

  @Override
  public Vec set(int i, double val) {
    return set(i / columns, i % columns, val);
  }

  @Override
  public Vec adjust(int i, double increment) {
    return adjust(i / columns, i % columns, increment);
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
    return rows;
  }


  private int getIdx(final int j) {
    return binaryLessOrEqSearch(indices, 0, Math.min(indices.length, j + 1), j);
  }

  private static int binaryLessOrEqSearch(int[] a, int fromIndex, int toIndex,
                                   int key) {
    int low = fromIndex;
    int high = toIndex - 1;

    while (low <= high) {
      int mid = (low + high) >>> 1;
      int midVal = a[mid];

      if (midVal < key)
        low = mid + 1;
      else if (midVal > key)
        high = mid - 1;
      else
        return mid; // key found
    }
    return low - 1;
  }



  private class ColsVecMxIterator implements MxIterator {
    private VecIterator cIter;
    private int columnBlock = 0;
    private int row = 0;

    ColsVecMxIterator() {
      cIter = vec.at(0).at(0).nonZeroes();
    }

    @Override
    public int column() {
      return indices[columnBlock] + row;
    }

    @Override
    public int row() {
      return cIter.index();
    }

    @Override
    public int index() {
      return cIter.index() * columns() + indices[columnBlock] + row;
    }

    @Override
    public double value() {
      return cIter.value();
    }

    @Override
    public boolean isValid() {
      return columnBlock < vec.length() && row < vec.at(columnBlock).length() && cIter.isValid();
    }

    @Override
    public boolean advance() {
      if (!cIter.advance()) {
        boolean ok = false;
        while (!ok) {
          ++row;
          if (row >= vec.at(columnBlock).length()) {
            row = 0;
            ++columnBlock;
            if (columnBlock >= vec.length()) {
              return false;
            }
          }
          cIter = vec.at(columnBlock).at(row).nonZeroes();
          ok = cIter.advance();
        }
      }
      return true;
    }

    @Override
    public boolean seek(final int pos) {
      throw new NotImplementedException();
    }

    @Override
    public double setValue(final double v) {
      return cIter.setValue(v);
    }
  }

}
