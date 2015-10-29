package com.spbsu.commons.seq;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * User: qdeee
 * Date: 08.09.14
 */
public class IntSeqBuilder implements SeqBuilder<Integer> {
  private final TIntList data;

  public IntSeqBuilder() {
    data = new TIntArrayList();
  }

  public SeqBuilder<Integer> append(final int value) {
    data.add(value);
    return this;
  }

  public SeqBuilder<Integer> append(final int... value) {
    data.add(value);
    return this;
  }

  @Override
  public SeqBuilder<Integer> add(final Integer integer) {
    append(integer);
    return this;
  }

  @Override
  public SeqBuilder<Integer> addAll(final Seq<Integer> values) {
    for (int i = 0; i < values.length(); i++) {
      append(values.at(i));
    }
    return this;
  }

  @Override
  public IntSeq build() {
    return new IntSeq(data.toArray());
  }

  public int length() {
    return data.size();
  }
}
