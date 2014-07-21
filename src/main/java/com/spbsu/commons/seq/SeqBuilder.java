package com.spbsu.commons.seq;

public interface SeqBuilder<T> {
  SeqBuilder<T> add(T t);
  SeqBuilder<T> addAll(Seq<T> values);

  Seq<T> build();
}