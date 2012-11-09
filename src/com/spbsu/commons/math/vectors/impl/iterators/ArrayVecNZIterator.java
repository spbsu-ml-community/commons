package com.spbsu.commons.math.vectors.impl.iterators;

import com.spbsu.commons.math.vectors.impl.ArrayVec;
import com.spbsu.commons.math.vectors.VecIterator;

/**
* User: solar
* Date: 10/9/12
* Time: 9:01 AM
*/
public class ArrayVecNZIterator implements VecIterator {
  int index;
  int dim;
  double[] values;

  public ArrayVecNZIterator(ArrayVec arrayVec) {
    this.values = arrayVec.values;
    this.dim = arrayVec.dim();
    index = -1;
  }

  public ArrayVecNZIterator(double[] values) {
    this.values = values;
    this.dim = values.length;
    index = -1;
  }

  @Override
  public int index() {
    return index;
  }

  @Override
  public double value() {
    return values[index];
  }

  @Override
  public boolean isValid() {
    return index < dim && index >= 0;
  }

  @Override
  public boolean seek(int pos) {
    index = pos - 1;
    return isValid();
  }

  @Override
  public boolean advance() {
    while(++index < dim && values[index] == 0);
    return isValid();
  }

  @Override
  public double setValue(double v) {
    values[index] = v;
    return v;
  }

}
