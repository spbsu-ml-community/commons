package com.spbsu.commons.math.vectors;

/**
 * User: solar
 * Date: 26.07.12
 * Time: 20:53
 */
public interface MxIterator extends VecIterator {
  int column();
  int row();

  double value();

  boolean isValid();
  boolean advance();

  double setValue(double v);

  int index();
}
