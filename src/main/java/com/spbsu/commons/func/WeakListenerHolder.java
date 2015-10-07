package com.spbsu.commons.func;

/**
 * User: solar
 * Date: 18.11.13
 * Time: 19:34
 */
public interface WeakListenerHolder<Event> {
  /** listener will be referenced by WEAK reference due to leaks resistance */
  void addListener(Action<? super Event> lst);
  void removeListener(Action<? super Event> action);
}
