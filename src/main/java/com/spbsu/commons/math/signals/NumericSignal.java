package com.spbsu.commons.math.signals;

/**
 * @author vp
 */
public interface NumericSignal<T extends Number> extends Signal<T> {
  T[] getValues();
}
