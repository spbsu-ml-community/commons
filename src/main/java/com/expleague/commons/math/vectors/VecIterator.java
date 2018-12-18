package com.expleague.commons.math.vectors;

import gnu.trove.procedure.TIntDoubleProcedure;

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

  default boolean advance(int index, TIntDoubleProcedure todo) {
    todo.execute(index(), value());
    while(advance()) {
      if (index() >= index)
        return true;
      todo.execute(index(), value());
    }
    return false;
  }

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
