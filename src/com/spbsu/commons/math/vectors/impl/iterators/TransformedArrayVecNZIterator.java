package com.spbsu.commons.math.vectors.impl.iterators;

import com.spbsu.commons.math.vectors.IndexTransformation;

/**
* User: solar
* Date: 10/9/12
* Time: 9:44 AM
*/
public class TransformedArrayVecNZIterator extends ArrayVecNZIterator {
  private final IndexTransformation trans;

  public TransformedArrayVecNZIterator(double[] values, IndexTransformation trans) {
    super(values);
    dim = trans.newDim();
    this.trans = trans;
  }

  @Override
  public boolean advance() {
    while(++index < dim && value() == 0);
    return isValid();
  }

  @Override
  public double value() {
    return values[trans.forward(index)];
  }

  @Override
  public double setValue(double v) {
    values[trans.forward(index)] = v;
    return v;
  }

}
