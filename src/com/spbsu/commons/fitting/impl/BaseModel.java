package com.spbsu.commons.fitting.impl;

import com.spbsu.commons.fitting.Factor;
import com.spbsu.commons.fitting.Model;

/**
 * @author vp
 */
public abstract class BaseModel extends Model {
  private final Factor<? extends Number>[] factors;

  protected BaseModel(final Factor<? extends Number>... factors) {
    this.factors = factors;
  }

  public final Factor<? extends Number>[] getFactors() {
    return factors;
  }
}
