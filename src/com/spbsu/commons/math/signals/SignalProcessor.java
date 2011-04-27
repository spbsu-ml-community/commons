package com.spbsu.commons.math.signals;

/**
 * User: terry
 * Date: 10.12.2009
 */
public interface SignalProcessor<T> {
  void process(long timestamp, T value);
}
