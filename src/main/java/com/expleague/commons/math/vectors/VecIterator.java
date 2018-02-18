package com.expleague.commons.math.vectors;

import java.util.Iterator;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 17:24:40
 */
public interface VecIterator extends Iterator<Double> {
  int index();
  double value();

  boolean isValid();
  boolean advance();
  boolean seek(int pos);

  double setValue(double v);

  @Override
  default boolean hasNext() {
    return isValid();
  }

  @Override
  default Double next() {
    double value = value();
    advance();
    return value;
  }
}
