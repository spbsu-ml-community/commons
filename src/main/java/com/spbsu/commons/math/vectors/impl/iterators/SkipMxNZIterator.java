package com.spbsu.commons.math.vectors.impl.iterators;

import com.spbsu.commons.math.vectors.Mx;
import com.spbsu.commons.math.vectors.MxIterator;
import com.spbsu.commons.math.vectors.VecIterator;

/**
* User: solar
* Date: 10/10/12
* Time: 10:01 PM
*/
public class SkipMxNZIterator extends SkipVecNZIterator implements MxIterator {
  private final int columns;
  public SkipMxNZIterator(Mx mx) {
    super(mx);
    columns = mx.columns();
  }

  @Override
  public int column() {
    return index() % columns;
  }

  @Override
  public int row() {
    return index() / columns;
  }
}
