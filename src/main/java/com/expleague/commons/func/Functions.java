package com.expleague.commons.func;

import java.io.IOException;
import java.util.function.Consumer;
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

  public interface FunctionRethrows<A, R> {
    R apply(A a) throws Exception;
  }

  public interface ConsumerRethrows<A> {
    void apply(A a) throws Exception;
  }

  public static <A, R> Function<A, R> rethrow(FunctionRethrows<A, R> f) {
    return a -> {
      try {
        return f.apply(a);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  public static <A> Consumer<A> rethrow(ConsumerRethrows<A> f) {
    return a -> {
      try {
        f.apply(a);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  public static <A, B> Function<B, A> cast(Class<A> clazz) {
    return x -> clazz.isAssignableFrom(x.getClass()) ? (A)x : null;
  }
}
