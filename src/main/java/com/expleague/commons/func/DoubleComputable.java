package com.expleague.commons.func;

import java.util.function.Function;

/**
 * User: starlight
 * Date: 16.02.15
 */
public abstract class DoubleComputable<T> implements Function<T, Double> {
  @Override
  public Double apply(final T argument) {
    return computeValue(argument);
  }

  public abstract double computeValue(final T t);
}
