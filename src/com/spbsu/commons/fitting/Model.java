package com.spbsu.commons.fitting;

/**
 * @author vp
 */
public abstract class Model {
  public abstract double evaluate(final Candidate candidate);
  public abstract Factor<? extends Number>[] getFactors();
}
