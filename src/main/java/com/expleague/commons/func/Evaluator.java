package com.expleague.commons.func;

import java.util.function.ToDoubleFunction;

/**
 * User: solar
 * Date: 12.11.13
 * Time: 16:26
 */
public interface Evaluator<T> extends ToDoubleFunction<T> {
  double value(T t);

  @Override
  default double applyAsDouble(T value) {
    return value(value);
  }
}
