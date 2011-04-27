package com.spbsu.commons.math.signals.generic;

import com.spbsu.commons.util.Factories;

import java.util.Set;

/**
 * User: terry
 * Date: 13.12.2009
 */
public class HashSetAdditiveSignal<T> extends SetAdditiveSignal<T> {
  @Override
  protected Set<T> createSet() {
    return Factories.hashSet();
  }
}
