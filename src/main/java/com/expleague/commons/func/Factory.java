package com.expleague.commons.func;

import java.util.function.Supplier;

/**
 * User: solar
 * Date: 24.06.13
 * Time: 12:28
 */
public interface Factory<T> extends Supplier<T> {
  T create();

  default T get() {
    return create();
  }
}
