package com.spbsu.commons.seq;

/**
 * User: Manokk
 * Date: 31.08.11
 * Time: 2:45
 */
public interface Seq<T> {
  T at(int i);
  Seq<T> sub(int start, int end);
  int length();
  boolean isImmutable();
  Class<? extends T> elementType();

  abstract class Stub<T> implements Seq<T>{
    @Override
    public Seq<T> sub(final int start, final int end) {
      if (end > length())
        throw new ArrayIndexOutOfBoundsException();
      return new Stub<T>() {
        @Override
        public T at(final int i) {
          if (start + i >= end)
            throw new ArrayIndexOutOfBoundsException();
          return Stub.this.at(start + i);
        }

        @Override
        public int length() {
          return end - start;
        }

        @Override
        public boolean isImmutable() {
          return Stub.this.isImmutable();
        }

        @Override
        public Class<? extends T> elementType() {
          return Stub.this.elementType();
        }
      };
    }

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
  }
}