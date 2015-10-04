package com.spbsu.commons.io.codec.seq;

import com.spbsu.commons.seq.Seq;

import java.util.Collections;
import java.util.List;

/**
 * User: solar
 * Date: 30.09.15
 * Time: 18:39
 */
public interface Dictionary<T extends Comparable<T>> {
  Dictionary EMPTY = new Dictionary() {
    @Override
    public int search(Seq seq) {
      return -1;
    }

    @Override
    public Seq get(int index) {
      return null;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public List<? extends Seq> alphabet() {
      return Collections.emptyList();
    }

    @Override
    public int parent(int second) {
      return -1;
    }
  };

  int search(Seq<T> seq);

  Seq<T> get(int index);

  int size();

  List<? extends Seq<T>> alphabet();

  int parent(int second);
}
