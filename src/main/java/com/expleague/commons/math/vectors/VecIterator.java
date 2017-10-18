package com.expleague.commons.math.vectors;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 17:24:40
 */
public interface VecIterator {
  int index();
  double value();

  boolean isValid();
  boolean advance();
  boolean seek(int pos);

  double setValue(double v);
}
