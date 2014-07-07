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

  abstract static class Stub<T> implements Seq<T>{
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
      };
    }
  }
}