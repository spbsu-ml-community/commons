package com.spbsu.commons.math.signals.generic;

import com.spbsu.commons.math.signals.DynamicSignal;
import com.spbsu.commons.math.signals.Signal;
import com.spbsu.commons.math.signals.SignalProcessor;
import com.spbsu.commons.util.Factories;

import java.util.List;

/**
 * User: terry
 * Date: 13.12.2009
 */
public class GenericSignal<T> extends BaseSignal<T> implements Signal<T>, DynamicSignal<T> {
  private final List<T> values;

  public GenericSignal() {
    super();
    values = Factories.arrayList();
  }

  public GenericSignal(long[] timestamps, T[] values) {
    super(timestamps);
    this.values = Factories.arrayList(values);
  }

  public T getValue(long timestamp) {
    int index = 0;
    for (; index < timestamps.size(); index++) {
      if (timestamps.getQuick(index) == timestamp) {
        return values.get(index);
      }
    }
    return null;
  }

  @Override
  public Object[] getValues() {
    return values.toArray();
  }

  @Override
  public T getValue(int index) {
    return values.get(index);
  }

  @Override
  public void process(SignalProcessor<T> signalProcessor) {
    int index = 0;
    for (final T value : values) {
      signalProcessor.process(timestamps.getQuick(index++), value);
    }
  }

  @Override
  public void occur(long timestamp, T value) {
    int index = 0;
    for (; index < timestamps.size(); index++) {
      if (timestamps.getQuick(index) > timestamp) break;
    }
    if (!(index > 0 && timestamps.get(index - 1) == timestamp)) {
      timestamps.insert(index, timestamp);
      values.add(index, value);
    } else {
      values.remove(index - 1);
      values.add(index - 1, value);
    }
  }
}
