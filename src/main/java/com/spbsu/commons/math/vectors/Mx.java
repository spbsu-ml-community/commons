package com.spbsu.commons.math.vectors;

import com.spbsu.commons.math.vectors.impl.basis.MxBasisImpl;
import com.spbsu.commons.math.vectors.impl.idxtrans.SubMxTransformation;
import com.spbsu.commons.math.vectors.impl.iterators.MxIteratorImpl;
import com.spbsu.commons.math.vectors.impl.iterators.SkipMxNZIterator;
import com.spbsu.commons.math.vectors.impl.vectors.IndexTransVec;

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
  }
}
