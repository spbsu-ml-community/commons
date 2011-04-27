package com.spbsu.commons.fitting;

/**
 * @author vp
 */
public interface Factor<T extends Number> {
  public T getLowBound();
  public T getUpBound();
  public void setValue(final T value);
}
