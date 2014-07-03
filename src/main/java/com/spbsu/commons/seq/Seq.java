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
}