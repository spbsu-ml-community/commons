package com.expleague.commons.math.vectors.impl.vectors;

import com.expleague.commons.math.MathTools;
import com.expleague.commons.math.vectors.VecIterator;
import com.expleague.commons.math.vectors.SingleValueVec;
import com.expleague.commons.math.vectors.Vec;

/**
 * User: qdeee
 * Date: 21.05.17
 */
public class SingleElemVec extends Vec.Stub {
  private final int pos;
  private final double value;
  private final int dim;

  public SingleElemVec(int pos, double value, int dim) {
    if (pos >= dim) {
      throw new IndexOutOfBoundsException();
    }

    this.pos = pos;
    this.value = value;
    this.dim = dim;
  }

  @Override
  public double get(int i) {
    if (i < 0 || i >= dim) {
      throw new IndexOutOfBoundsException();
    }

    return i == pos ? value : 0;
  }

  @Override
  public Vec set(int i, double val) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Vec adjust(int i, double increment) {
    throw new UnsupportedOperationException();
  }

  @Override
  public VecIterator nonZeroes() {
    return new VecIterator() {
      int index = -1;

      @Override
      public int index() {
        return index;
      }

      @Override
      public double value() {
        if (index == pos) {
          return value;
        } else {
          throw new IllegalStateException("Attempted to obtain value different from the only element");
        }
      }

      @Override
      public boolean isValid() {
        return index == pos;
      }

      @Override
      public boolean advance() {
        if (index < pos && Math.abs(value) > MathTools.EPSILON) {
          index = pos;
        } else {
          index = dim;
        }
        return isValid();
      }

      @Override
      public boolean seek(int pos) {
        return (index = pos) < dim;
      }

      @Override
      public double setValue(double v) {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public int dim() {
    return dim;
  }

  @Override
  public Vec sub(int start, int len) {
    if (start <= pos && pos < start + len) {
      return new SingleElemVec(pos - start, value, dim - len);
    } else {
      return new SingleValueVec(0, len);
    }
  }

  @Override
  public boolean isImmutable() {
    return true;
  }
}
