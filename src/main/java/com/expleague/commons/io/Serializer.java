package com.expleague.commons.io;

import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * User: amosov-f
 * Date: 19.03.15
 * Time: 14:52
 */
public interface Serializer<T> {
  void serialize(@NotNull final T object, @NotNull final OutputStream out) throws IOException;

  @NotNull
  <R extends T> R deserialize(@NotNull final InputStream in, @NotNull final Class<R> clazz) throws IOException;
  
  // TODO java8: default methods
  abstract class Base<T> implements Serializer<T> {
    @NotNull
    public final byte[] toBytes(@NotNull final T object) {
      final ByteArrayOutputStream bout = new ByteArrayOutputStream();
      try {
        serialize(object, bout);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return bout.toByteArray();
    }

    @NotNull
    public final InputStream toInputStream(@NotNull final T object) {
      return new ByteArrayInputStream(toBytes(object));
    }
    
    @NotNull
    public final <R extends T> R fromBytes(@NotNull final byte[] bytes, @NotNull final Class<R> clazz) {
      try {
        return deserialize(new ByteArrayInputStream(bytes), clazz);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
