package com.spbsu.commons.seq;

public interface SeqBuilder<T> {
  SeqBuilder<T> add(T t);
  Seq<T> build();
}