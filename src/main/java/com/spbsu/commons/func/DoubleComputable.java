package com.spbsu.commons.func;

/**
 * User: starlight
 * Date: 16.02.15
 */
public abstract class DoubleComputable<T> implements Computable<T, Double> {
  @Override
  public Double compute(final T argument) {
    return computeValue(argument);
  }

  public abstract double computeValue(final T t);
}
