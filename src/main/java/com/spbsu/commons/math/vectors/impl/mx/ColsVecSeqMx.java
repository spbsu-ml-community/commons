package com.spbsu.commons.math.vectors.impl.mx;

import com.spbsu.commons.math.vectors.Mx;
import com.spbsu.commons.math.vectors.MxIterator;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.impl.idxtrans.SubMxTransformation;
import com.spbsu.commons.math.vectors.impl.iterators.SkipVecNZIterator;
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
      totalColumns += vecSeq.length() == 1 ? 1 : vecSeq.at(0).length();
    }
    columns = totalColumns;

    this.vec = new ArraySeq<VecSeq>(vecSeqs);

    this.rows = this.vec.at(0).length() == 1 ?
        this.vec.at(0).at(0).length() :
        this.vec.at(0).length();
  }

  public double get(int i, int j) {
    final int idx = getIdx(j);
    if (vec.at(idx).length() == 1)
      return vec.at(idx).at(0).get(i);
    else
      return vec.at(idx).at(i).get(j - indices[idx]);
  }

  @Override
  public Mx set(int i, int j, double val) {
    final int idx = getIdx(j);
    if (vec.at(idx).length() == 1)
      vec.at(idx).at(0).set(i, val);
    else
      vec.at(idx).at(i).set(j - indices[idx], val);
    return this;
  }

  @Override
  public Mx adjust(int i, int j, double increment) {
    final int idx = getIdx(j);
    if (vec.at(idx).length() == 1)
      vec.at(idx).at(0).adjust(i, increment);
    else
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
      private int idx = -1;
      @Override
      public VecIterator nonZeroes() {
        return new VecIterator() {
          private VecIterator iter = null;
          @Override
          public int index() {
            return col;
          }

          @Override
          public double value() {
            if (vec.at(idx).length() == 1)
              return vec.at(idx).at(0).get(i);
            return vec.at(idx).at(i).get(col - indices[idx]);
          }

          @Override
          public boolean isValid() {
            return col >= 0 && col < columns();
          }

          @Override
          public boolean advance() {
            if (iter != null) {
              int prev = iter.index();
              if (iter.advance()) {
                col += iter.index() - prev;
                return true;
              } else {
                iter = null;
                nextCol();
                --col;
                --idx;
              }
            }

            increaseCol();
            while (isValid() && value() == 0) {
              if (vec.at(idx).length() == 1)
                increaseCol();
              else {
                iter = vec.at(idx).at(i).nonZeroes();
                while (iter.advance() && col >= indices[idx] + iter.index());
                if (iter.isValid()) {
                  col = indices[idx] + iter.index();
                  return true;
                } else {
                  iter = null;
                  nextCol();
                }

              }
            }

            return isValid();
          }

          @Override
          public boolean seek(int pos) {
            throw new NotImplementedException();
          }

          @Override
          public double setValue(double v) {
            if (iter != null)
              return iter.setValue(v);
            final int idx = getIdx(col);
            return vec.at(idx).at(col - indices[idx]).set(i, v).get(i);
          }

          private void increaseCol() {
            ++col;
            if (idx < indices.length - 1 && col >= indices[idx + 1]) {
              ++idx;
            }
          }

          private void nextCol() {
            ++idx;
            col = idx < indices.length ? indices[idx] : length() + 2;
          }
        };
      }
    };
  }

  @Override
  public Vec col(final int j) {
    final int idx = getIdx(j);
    if (vec.at(idx).length() == 1)
      return vec.at(idx).at(0);

    return new Vec.Stub() {
      @Override
      public double get(int i) {
        return vec.at(idx).at(i).get(j - indices[idx]);
      }

      @Override
      public Vec set(int i, double val) {
        return vec.at(idx).at(i).set(j - indices[idx], val);
      }

      @Override
      public Vec adjust(int i, double increment) {
        return vec.at(idx).at(i).adjust(j - indices[idx], increment);
      }

      @Override
      public VecIterator nonZeroes() {
        return new SkipVecNZIterator(this);
      }

      @Override
      public int dim() {
        return rows();
      }

      @Override
      public Vec sub(int start, int len) {
        throw new NotImplementedException();
      }

      @Override
      public boolean isImmutable() {
        return false;
      }
    };
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
      return indices[columnBlock] + (vec.at(columnBlock).length() == 1 ? 0 : cIter.index());
    }

    @Override
    public int row() {
      return vec.at(columnBlock).length() == 1 ? cIter.index() : row;
    }

    @Override
    public int index() {
      return row() * columns() + column();
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
