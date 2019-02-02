package com.expleague.commons.func;

import java.util.function.ToIntFunction;

public interface HashFunction<T> extends ToIntFunction<T> {
  int hash(T v);
  int bits();

  default int applyAsInt(T value) {
    return hash(value);
  }
}
