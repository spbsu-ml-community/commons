package com.expleague.commons.math.vectors.impl.iterators;

import com.expleague.commons.math.vectors.VecIterator;
import com.expleague.commons.math.vectors.Vec;

/**
* User: solar
* Date: 16.04.14
* Time: 16:23
*/
public class SimpleVecIterator implements VecIterator {
  private final Vec vec;
  int index = 0;

  public SimpleVecIterator(final Vec vec) {
    this.vec = vec;
  }

  @Override
  public int index() {
    return index;
  }

  @Override
  public double value() {
    return vec.get(index);
  }

  @Override
  public boolean isValid() {
    return index < vec.dim();
  }

  @Override
  public boolean advance() {
    return ++index < vec.dim();
  }

  @Override
  public boolean seek(final int pos) {
    return (index = pos) < vec.dim();
  }

  @Override
  public double setValue(final double v) {
    return vec.set(index, v).get(index);
  }
}
