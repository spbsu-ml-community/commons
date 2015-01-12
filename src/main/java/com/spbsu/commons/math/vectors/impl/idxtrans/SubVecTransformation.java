package com.spbsu.commons.math.vectors.impl.idxtrans;

import com.spbsu.commons.math.vectors.IndexTransformation;

/**
 * User: solar
 * Date: 10/10/12
 * Time: 9:49 PM
 */
public class SubVecTransformation implements IndexTransformation {
  public final int start;
  public final int length;

  public SubVecTransformation(final int start, final int length) {
    this.start = start;
    this.length = length;
  }

  @Override
  public int forward(final int newIndex) {
    return start + newIndex;
  }

  @Override
  public int backward(final int oldIndex) {
    if (oldIndex < start || oldIndex >= start + length)
      return -1;
    return oldIndex - start;
  }

  @Override
  public int newDim() {
    return length;
  }

  @Override
  public int oldIndexStartHint() {
    return start;
  }

  @Override
  public int oldIndexEndHint() {
    return start + length;
  }

  @Override
  public IndexTransformation apply(final IndexTransformation trans) {
    return new CompositeTransformation(new IndexTransformation[]{this, trans});
  }
}
