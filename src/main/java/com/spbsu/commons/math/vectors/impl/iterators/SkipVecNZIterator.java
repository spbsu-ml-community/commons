package com.spbsu.commons.math.vectors.impl.iterators;

import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;

/**
* User: solar
* Date: 10/9/12
* Time: 9:01 AM
*/
public class SkipVecNZIterator implements VecIterator {
  protected final Vec vec;
  int index;

  public SkipVecNZIterator(final Vec arrayVec) {
    this.vec = arrayVec;
    index = -1;
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
    return index < vec.dim() && index >= 0;
  }

  @Override
  public boolean seek(final int pos) {
    index = pos - 1;
    return isValid();
  }

  @Override
  public boolean advance() {
    while(++index < vec.dim() && vec.get(index) == 0);
    return isValid();
  }

  @Override
  public double setValue(final double v) {
    vec.set(index, v);
    return v;
  }
}
