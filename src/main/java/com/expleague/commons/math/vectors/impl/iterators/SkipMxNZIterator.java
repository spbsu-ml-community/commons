package com.expleague.commons.math.vectors.impl.iterators;

import com.expleague.commons.math.vectors.Mx;
import com.expleague.commons.math.vectors.MxIterator;

/**
* User: solar
* Date: 10/10/12
* Time: 10:01 PM
*/
public class SkipMxNZIterator extends SkipVecNZIterator implements MxIterator {
  private final int columns;
  public SkipMxNZIterator(final Mx mx) {
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
