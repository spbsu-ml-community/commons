package com.spbsu.commons.math.signals.generic;

import com.spbsu.commons.math.signals.Signal;
import com.spbsu.commons.util.time.TimeFrame;
import gnu.trove.TLongArrayList;

/**
 * @author vp
 */
public abstract class BaseSignal<T> implements Signal<T> {
  protected final TLongArrayList timestamps;

  protected BaseSignal() {
    timestamps = new TLongArrayList(1);
  }

  protected BaseSignal(final long[] timestamps) {
    this.timestamps = new TLongArrayList(timestamps);
  }

  protected BaseSignal(final int timestampCount) {
    this.timestamps = new TLongArrayList(timestampCount);
  }

  @Override
  public int getTimestampCount() {
    return timestamps.size();
  }

  @Override
  public TimeFrame getTimeFrame() {
    if (timestamps.isEmpty()) return TimeFrame.EMPTY;
    return new TimeFrame(timestamps.get(0), timestamps.get(timestamps.size() - 1));
  }

  @Override
  public long[] getTimestamps() {
    return timestamps.toNativeArray();
  }

  @Override
  public long getTimestamp(final int index) {
    return timestamps.getQuick(index);
  }
}
