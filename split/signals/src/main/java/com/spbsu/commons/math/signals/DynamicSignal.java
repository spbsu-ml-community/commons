package com.spbsu.commons.math.signals;

/**
 * User: terry
 * Date: 13.12.2009
 */
public interface DynamicSignal<T> {
  void occur(long timestamp, T value);
}
