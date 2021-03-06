package com.expleague.commons.math.vectors;

import com.expleague.commons.math.vectors.impl.idxtrans.SubMxTransformation;
import com.expleague.commons.math.vectors.impl.mx.VecBasedMx;
import com.expleague.commons.math.vectors.impl.vectors.IndexTransVec;
import com.expleague.commons.math.vectors.impl.basis.MxBasisImpl;
import com.expleague.commons.math.vectors.impl.iterators.SkipMxNZIterator;

/**
 * User: solar
 * Date: 26.07.12
 * Time: 20:52
 */
public interface Mx extends Vec {
  double get(int i, int j);
  Mx set(int i, int j, double val);
  Mx adjust(int i, int j, double increment);

  Mx sub(int i, int j, int height, int width);
  Vec row(int i);
  Vec col(int j);

  @Override
  MxIterator nonZeroes();
  MxBasis basis();

  int columns();
  int rows();

  Vec vec();

  @SuppressWarnings("EqualsAndHashcode")
  abstract class Stub extends Vec.Stub implements Mx {
    @Override
    public final boolean equals(final Object obj) {
      return obj instanceof Mx && VecTools.equals(this, (Mx)obj);
    }

    @Override
    public final Vec sub(final int start, final int end) {
      throw new UnsupportedOperationException("Sub operation is not valid for matrices");
    }

    @Override
    public Vec row(final int i) {
      return new IndexTransVec(this, new SubMxTransformation(columns(), i, 0, 1, columns()));
    }

    @Override
    public Vec col(final int j) {
      return new IndexTransVec(this, new SubMxTransformation(columns(), 0, j, rows(), 1));
    }

    @Override
    public final int dim() {
      return columns() * rows();
    }

    @Override
    public MxIterator nonZeroes() {
      return new SkipMxNZIterator(this);
    }

    @Override
    public MxBasis basis() {
      return new MxBasisImpl(rows(), columns());
    }

    @Override
    public double get(int i, int j) {
      return get(i * columns() + j);
    }

    @Override
    public Mx set(int i, int j, double val) {
      set(i * columns() + j, val);
      return this;
    }

    @Override
    public Mx adjust(int i, int j, double increment) {
      adjust(i * columns() + j, increment);
      return this;
    }

    @Override
    public Mx sub(int i, int j, int height, int width) {
      return new VecBasedMx(width, new IndexTransVec(this, new SubMxTransformation(columns(), i, j, height, width)));
    }

    public Vec vec() {
      return this;
    }
  }
}
