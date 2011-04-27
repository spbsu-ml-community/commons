package com.spbsu.commons.fitting.impl;

import com.spbsu.commons.fitting.Factor;

/**
 * @author vp
 */
public abstract class BaseFactor<T extends Number> implements Factor<T> {
  private final T lowBound;
  private final T upBound;

  protected BaseFactor(final T lowBound, final T upBound) {
    this.lowBound = lowBound;
    this.upBound = upBound;
  }

  public final T getLowBound() {
    return lowBound;
  }

  public final T getUpBound() {
    return upBound;
  }
}
