package com.expleague.commons.func;

import java.util.function.Consumer;

/**
 * User: solar
 * Date: 18.11.13
 * Time: 19:34
 */
public interface WeakListenerHolder<Event> {
  /** listener will be referenced by WEAK reference due to leaks resistance */
  void addListener(Consumer<? super Event> lst);
  void removeListener(Consumer<? super Event> action);
}
