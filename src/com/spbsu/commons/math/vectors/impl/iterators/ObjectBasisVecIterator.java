package com.spbsu.commons.math.vectors.impl.iterators;

import com.spbsu.commons.math.vectors.BasisVecIterator;
import com.spbsu.commons.math.vectors.GenericBasis;
import com.spbsu.commons.math.vectors.IndexTransformation;
import com.spbsu.commons.math.vectors.VecIterator;

/**
* User: solar
* Date: 10/9/12
* Time: 9:07 AM
*/
public class ObjectBasisVecIterator<T> implements BasisVecIterator<T> {
  GenericBasis<T> basis;
  final VecIterator iter;

  public ObjectBasisVecIterator(VecIterator iter, GenericBasis<T> basis) {
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
  public boolean seek(int pos) {
    return iter.seek(pos);
  }

  @Override
  public double setValue(double v) {
    return iter.setValue(v);
  }
}
