package com.spbsu.commons.math.signals.numeric;

import com.spbsu.commons.math.signals.SignalProcessor;
import gnu.trove.TIntArrayList;

/**
 * @author vp
 */
public class BinarySignal extends IntSignal {
  private static final TIntArrayList EMPTY = new TIntArrayList(0);

  public BinarySignal() {
    super(EMPTY);
  }

  public BinarySignal(long[] timestamps) {
    super(timestamps, EMPTY);
  }

  @Override
  public int[] getNativeValues() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Integer[] getValues() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Integer getValue(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void process(SignalProcessor<Integer> signalProcessor) {
    for (int i = 0; i < timestamps.size(); i++) {
      signalProcessor.process(timestamps.getQuick(i), 1);
    }
  }

  public void occur(long timestamp) {
    occur(timestamp, 1);
  }

  @Override
  protected void insertValue(int index, Integer value) {
    //nothing
  }

  @Override
  protected void adjustValue(int index, Integer value) {
    //nothing
  }

  @Override
  public int sum() {
    return timestamps.size();
  }
}
