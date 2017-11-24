package com.expleague.commons.math.vectors.impl.vectors;

import com.expleague.commons.seq.SeqBuilder;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.seq.Seq;
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

  public VecBuilder addAll(final VecBuilder other) {
    data.addAll(other.data);
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
