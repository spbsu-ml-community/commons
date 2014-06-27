package com.spbsu.commons.math.signals.generic;

import com.spbsu.commons.math.signals.AdditiveSignal;

import java.util.Set;

/**
 * User: terry
 * Date: 13.12.2009
 */
public abstract class SetAdditiveSignal<T> implements AdditiveSignal<T, Set<T>> {
  private final GenericSignal<Set<T>> genericSignal;

  public SetAdditiveSignal() {
    genericSignal = new GenericSignal<Set<T>>();
  }

  protected abstract Set<T> createSet();

  @Override
  public GenericSignal<Set<T>> getSignal() {
    return genericSignal;
  }

  @Override
  public void occur(long timestamp, T value) {
    Set<T> set = genericSignal.getValue(timestamp);
    if (set == null) {
      genericSignal.occur(timestamp, set = createSet());
    }
    set.add(value);
  }
}
