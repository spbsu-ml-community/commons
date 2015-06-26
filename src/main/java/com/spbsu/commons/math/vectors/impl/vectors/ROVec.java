package com.spbsu.commons.math.vectors.impl.vectors;

import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.impl.iterators.SkipVecNZIterator;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:25:41
 */
public class ROVec extends Vec.Stub {
  public final Vec delegate;
  public ROVec(Vec delegate) {
    this.delegate = delegate;
  }
  @Override
  public int dim() {
    return delegate.dim();
  }

  @Override
  public double[] toArray() {
    return delegate.toArray();
  }

  @Override
  public void toArray(final double[] src, final int offset) {
    delegate.toArray(src, offset);
  }

  @Override
  public double get(final int i) {
    return delegate.get(i);
  }

  @Override
  public Vec set(final int i, final double val) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Vec adjust(final int i, final double increment) {
    throw new UnsupportedOperationException();
  }

  @Override
  public VecIterator nonZeroes() {
    return new SkipVecNZIterator(delegate){
      @Override
      public double setValue(double v) {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public boolean isImmutable() {
    return true;
  }

  @Override
  public ROVec sub(final int start, final int length) {
    return new ROVec(delegate.sub(start, length));
  }
}
