package com.expleague.commons.math.vectors.impl.iterators;

import com.expleague.commons.math.vectors.BasisVecIterator;
import com.expleague.commons.math.vectors.VecIterator;
import com.expleague.commons.math.vectors.GenericBasis;

/**
* User: solar
* Date: 10/9/12
* Time: 9:07 AM
*/
public class ObjectBasisVecIterator<T> implements BasisVecIterator<T> {
  GenericBasis<T> basis;
  final VecIterator iter;

  public ObjectBasisVecIterator(final VecIterator iter, final GenericBasis<T> basis) {
    this.basis = basis;
    this.iter = iter;
  }

  @Override
  public int index() {
    return iter.index();
  }

  @Override
  public double value() {
    return iter.value();
  }

  @Override
  public T key() {
    return basis.fromIndex(iter.index());
  }

  @Override
  public boolean isValid() {
    return iter.isValid();
  }

  @Override
  public boolean advance() {
    return iter.advance();
  }

  @Override
  public boolean seek(final int pos) {
    return iter.seek(pos);
  }

  @Override
  public double setValue(final double v) {
    return iter.setValue(v);
  }
}
