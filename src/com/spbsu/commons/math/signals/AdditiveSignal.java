package com.spbsu.commons.math.signals;

import java.util.Collection;

/**
 * User: terry
 * Date: 12.12.2009
 */
public interface AdditiveSignal<T, C extends Collection<T>> extends DynamicSignal<T> {
  Signal<C> getSignal();
}
