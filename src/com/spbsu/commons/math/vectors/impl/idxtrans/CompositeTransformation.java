package com.spbsu.commons.math.vectors.impl.idxtrans;

import com.spbsu.commons.math.vectors.IndexTransformation;

/**
* User: solar
* Date: 10/10/12
* Time: 9:39 PM
*/
class CompositeTransformation implements IndexTransformation {
  private final IndexTransformation[] sequence;

  public CompositeTransformation(IndexTransformation[] sequence) {
    this.sequence = sequence;
  }

  @Override
  public int forward(int newIndex) {
    for (int i = 0; i < sequence.length; i++)
      newIndex = sequence[i].forward(newIndex);
    return newIndex;
  }

  @Override
  public int backward(int oldIndex) {
    for (int i = sequence.length - 1; i >= 0; i--)
      oldIndex = sequence[i].backward(oldIndex);
    return oldIndex;
  }

  @Override
  public int newDim() {
    return sequence[sequence.length - 1].newDim();
  }

  @Override
  public int oldIndexStartHint() {
    return sequence[0].oldIndexStartHint();
  }

  @Override
  public int oldIndexEndHint() {
    return sequence[0].oldIndexEndHint();
  }

  @Override
  public IndexTransformation apply(IndexTransformation trans) {
    IndexTransformation[] sequence = new IndexTransformation[this.sequence.length + 1];
    System.arraycopy(this.sequence, 0, sequence, 1, this.sequence.length);
    sequence[0] = trans;
    return new CompositeTransformation(sequence);
  }
}
