package com.spbsu.commons.math.vectors.impl.iterators;

import com.spbsu.commons.math.vectors.MxIterator;
import com.spbsu.commons.math.vectors.VecIterator;

/**
* User: solar
* Date: 10/10/12
* Time: 10:01 PM
*/
public class MxIteratorImpl implements MxIterator {
  private final VecIterator parent;
  private final int columns;

  public MxIteratorImpl(final VecIterator parent, final int columns) {
    this.parent = parent;
    this.columns = columns;
  }

  @Override
  public int column() {
    return parent.index() % columns;
  }

  @Override
  public int row() {
    return parent.index() / columns;
  }

  @Override
  public double value() {
    return parent.value();
  }

  @Override
  public boolean isValid() {
    return parent.isValid();
  }

  @Override
  public boolean advance() {
    return parent.advance();
  }

  @Override
  public boolean seek(final int pos) {
    return parent.seek(pos);
  }

  @Override
  public double setValue(final double v) {
    return parent.setValue(v);
  }

  @Override
  public int index() {
    return parent.index();
  }
}
