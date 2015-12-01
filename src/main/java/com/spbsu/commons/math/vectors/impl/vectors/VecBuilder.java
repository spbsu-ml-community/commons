package com.spbsu.commons.math.vectors.impl.vectors;

import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.seq.Seq;
import com.spbsu.commons.seq.SeqBuilder;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * User: solar
 * Date: 07.07.14
 * Time: 20:24
 */
public class VecBuilder implements SeqBuilder<Double> {
  private final TDoubleArrayList data;

  public VecBuilder() {
    data = new TDoubleArrayList();
  }

  public VecBuilder(final int capacity) {
    data = new TDoubleArrayList(capacity);
  }

  @Override
  public VecBuilder add(final Double aDouble) {
    append(aDouble);
    return this;
  }

  @Override
  public VecBuilder addAll(final Seq<Double> values) {
    for (int i = 0; i < values.length(); i++) {
      add(values.at(i));
    }
    return this;
  }

  public void append(final double d) {
    data.add(d);
  }

  @Override
  public Vec build() {
    return new ArrayVec(data.toArray());
  }

  @Override
  public void clear() {
    data.clear();
  }
}
