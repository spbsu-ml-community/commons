package com.spbsu.commons.math.signals;

import com.spbsu.commons.util.frame.time.TimeFrame;

/**
 * User: terry
 * Date: 10.12.2009
 */
public interface Signal<T> {
  int getTimestampCount();
  TimeFrame getTimeFrame();
  long[] getTimestamps();
  long getTimestamp(final int index);
  Object[] getValues();
  T getValue(final int index);
  void process(SignalProcessor<T> processor);
}
