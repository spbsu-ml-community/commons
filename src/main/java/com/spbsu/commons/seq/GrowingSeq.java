package com.spbsu.commons.seq;

import com.spbsu.commons.math.vectors.Vec;

/**
 * User: Manokk
 * Date: 31.08.11
 * Time: 2:45
 */
public interface GrowingSeq<T> extends Seq<T> {
  GrowingSeq<T> add(T val);
}