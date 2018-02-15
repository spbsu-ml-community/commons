package com.expleague.commons.seq;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.stream.BaseStream;

/**
 * User: Manokk
 * Date: 31.08.11
 * Time: 2:45
 */
public interface Seq<T> extends Iterable<T> {
  T at(int i);
  Seq<T> sub(int start, int end);
  int length();
  boolean isImmutable();
  Class<? extends T> elementType();

  <S extends BaseStream<T, S>> S stream();
  <A> A toArray();

  abstract class Stub<T> implements Seq<T> {
    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      final int length = length();
      builder.append('[');
      for (int i = 0; i < length; i++) {
        if (i != 0)
          builder.append(", ");
        builder.append(at(i).toString());
      }
      builder.append(']');
      return builder.toString();
    }

    private int hash = -1;
    @Override
    public int hashCode() {
      if (hash != -1)
        return hash;
      int result = length();
      result <<= 1;
      for (int i = 0; i < length(); i++) {
        result += at(i).hashCode();
        result <<= 2;
      }
      if (result == -1)
        result = 0;
      return hash = result;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this)
        return true;
      if (!(obj instanceof Seq))
        return false;
      final Seq other = (Seq)obj;
      if (other.length() != length())
        return false;
      for (int i = 0; i < length(); i++) {
        if (!at(i).equals(other.at(i)))
          return false;
      }
      return true;
    }

    @Override
    public <A> A toArray() {
      //noinspection unchecked
      final T[] array = (T[])Array.newInstance(elementType(), length());
      for (int i = 0; i < array.length; i++) {
        array[i] = at(i);
      }
      //noinspection unchecked
      return (A)array;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
      return new Iterator<T>() {
        private int pos = 0;

        @Override
        public boolean hasNext() {
          return pos < length();
        }

        @Override
        public T next() {
          return at(pos++);
        }
      };
    }
  }
}