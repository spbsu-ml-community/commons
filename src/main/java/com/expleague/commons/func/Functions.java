package com.expleague.commons.func;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author vpdelta
 */
public class Functions {
  private Functions() {}

  public static <T> Function<Object, Stream<T>> instancesOf(Class<T> cls) {
    return o -> cls.isInstance(o) ? Stream.of(cls.cast(o)) : Stream.empty();
  }
}
