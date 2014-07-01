package com.spbsu.commons.math.signals.numeric;

import com.spbsu.commons.math.signals.SignalProcessor;
import com.spbsu.commons.util.time.TimeFrame;

/**
 * @author vp
 */
public class WrappingBinarySignal extends BinarySignal {
  private IntSignal signal;

  public WrappingBinarySignal(final IntSignal signal) {
    this.signal = signal;
  }

  @Override
  public void process(SignalProcessor<Integer> signalProcessor) {
    signal.process(signalProcessor);
  }

  @Override
  public TimeFrame getTimeFrame() {
    return signal.getTimeFrame();
  }

  @Override
  public int getTimestampCount() {
    return signal.getTimestampCount();
  }

  @Override
  public long[] getTimestamps() {
    return signal.getTimestamps();
  }

  @Override
  protected void adjustValue(final int index, final Integer value) {
  }

  @Override
  public void occur(final long timestamp, final Integer value) {
    occur(timestamp);
  }

  @Override
  public void occur(final long timestamp) {
    signal.occur(timestamp, 1);
  }

  @Override
  protected void insertValue(final int index, final Integer value) {
    signal.insertValue(index, 1);
  }

  @Override
  public long getTimestamp(final int index) {
    return signal.getTimestamp(index);
  }

  @Override
  public long ceil(final long timestamp) {
    return signal.ceil(timestamp);
  }

  @Override
  public int ceilIndex(final long timestamp) {
    return signal.ceilIndex(timestamp);
  }

  @Override
  public long floor(final long timestamp) {
    return signal.floor(timestamp);
  }

  @Override
  public int floorIndex(final long timestamp) {
    return signal.floorIndex(timestamp);
  }

  @Override
  public int sum() {
    return signal.sum();
  }
}
