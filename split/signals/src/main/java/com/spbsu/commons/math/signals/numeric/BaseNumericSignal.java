package com.spbsu.commons.math.signals.numeric;

import com.spbsu.commons.math.signals.DynamicSignal;
import com.spbsu.commons.math.signals.NumericSignal;
import com.spbsu.commons.math.signals.SignalProcessor;
import com.spbsu.commons.math.signals.generic.BaseSignal;

/**
 * User: terry
 * Date: 13.12.2009
 */
public abstract class BaseNumericSignal<T extends Number> extends BaseSignal<T> implements NumericSignal<T>, DynamicSignal<T> {
  public BaseNumericSignal() {
    super();
  }

  public BaseNumericSignal(final long[] timestamps) {
    super(timestamps);
  }

  protected BaseNumericSignal(int timestampCount) {
    super(timestampCount);
  }

  @Override
  public void process(SignalProcessor<T> signalProcessor) {
    for (int index = 0; index < timestamps.size(); index++) {
      signalProcessor.process(timestamps.getQuick(index), getValue(index));
    }
  }

  @Override
  public void occur(final long timestamp, final T value) {
    final int size = timestamps.size();
    if (size == 0) {
      insertNewOccurence(0, timestamp, value);
      return;
    }

    final long last = timestamps.getQuick(size - 1);
    if (last < timestamp) {
      insertNewOccurence(size, timestamp, value);
      return;
    }
    else if (last == timestamp) {
      adjustValue(size - 1, value);
      return;
    }

    final int floorIndex = floorIndex(timestamp);
    if (floorIndex == -1) {
      insertNewOccurence(0, timestamp, value);
    }
    else {
      final long floor = timestamps.getQuick(floorIndex);
      if (floor == timestamp) {
        adjustValue(floorIndex, value);
      }
      else {
        insertNewOccurence(floorIndex + 1, timestamp, value);
      }
    }
  }

  private void insertNewOccurence(final int index, final long timestamp, final T value) {
    timestamps.insert(index, timestamp);
    insertValue(index, value);
  }

  protected abstract void insertValue(int index, T value);

  protected abstract void adjustValue(int index, T value);

  public long floor(final long timestamp) {
    final int index = floorIndex(timestamp);
    return index != -1 ? timestamps.getQuick(index) : -1;
  }
  
  public long ceil(final long timestamp) {
    final int index = ceilIndex(timestamp);
    return index != -1 ? timestamps.getQuick(index) : -1;
  }

  public int floorIndex(final long timestamp) {
    final int size = timestamps.size();

    final int firstIndex = 0;
    final int lastIndex = size - 1;

    int low = firstIndex;
    int high = lastIndex;

    while (low <= high) {
      final int mid = (low + high) >>> 1;

      final long midVal = timestamps.getQuick(mid);

      final int next = mid + 1;
      final int prev = mid - 1;

      if (midVal < timestamp) {
        if (mid >= lastIndex) {
          return lastIndex;
        }
        final long nextTimestamp = timestamps.getQuick(next);
        if (nextTimestamp < timestamp) {
          low = next;
        }
        else {
          if (nextTimestamp == timestamp) {
            return next;
          }
          else {
            return mid;
          }
        }
      }
      else if (midVal > timestamp) {
        if (mid == firstIndex) {
          return -1;
        }
        final long prevTimestamp = timestamps.getQuick(prev);
        if (prevTimestamp > timestamp) {
          high = prev;
        }
        else {
          if (prevTimestamp == timestamp) {
            return prev;
          }
          else {
            return prev;
          }
        }
      }
      else {
        return mid;
      }
    }
    throw new RuntimeException("Never is reached");
  }

  public int ceilIndex(final long timestamp) {
    final int size = timestamps.size();

    final int firstIndex = 0;
    final int lastIndex = size - 1;

    int low = firstIndex;
    int high = lastIndex;

    while (low <= high) {
      final int mid = (low + high) >>> 1;

      final long midVal = timestamps.getQuick(mid);

      final int next = mid + 1;
      final int prev = mid - 1;

      if (midVal < timestamp) {
        if (mid == lastIndex) {
          return -1;
        }
        final long nextTimestamp = timestamps.getQuick(next);
        if (nextTimestamp < timestamp) {
          low = next;
        }
        else {
          if (nextTimestamp == timestamp) {
            return next;
          }
          else {
            return next;
          }
        }
      }
      else if (midVal > timestamp) {
        if (mid == firstIndex) {
          return firstIndex;
        }
        final long prevTimestamp = timestamps.getQuick(prev);
        if (prevTimestamp > timestamp) {
          high = prev;
        }
        else {
          if (prevTimestamp == timestamp) {
            return prev;
          }
          else {
            return mid;
          }
        }
      }
      else {
        return mid;
      }
    }
    throw new RuntimeException("Never is reached");
  }
}
