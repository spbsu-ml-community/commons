package com.expleague.commons.math.vectors.impl.vectors;

import com.expleague.commons.math.MathTools;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.seq.Seq;
import com.expleague.commons.seq.SeqBuilder;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * User: solar
 * Date: 07.07.14
 * Time: 20:24
 */
public class SparseVecBuilder extends VecBuilder {
  private final TIntArrayList indices;
  int index = 0;

  public SparseVecBuilder() {
    indices = new TIntArrayList();
  }

  public SparseVecBuilder(final int capacity) {
    super(capacity);
    indices = new TIntArrayList(capacity);
  }

  @Override
  public SparseVecBuilder add(final Double aDouble) {
    append(aDouble);
    return this;
  }

  @Override
  public SparseVecBuilder addAll(final Seq<Double> values) {
    for (int i = 0; i < values.length(); i++) {
      add(values.at(i));
    }
    return this;
  }

  public void append(final double d) {
    if (d > MathTools.EPSILON) {
      data.add(d);
      indices.add(index);
    }
    index++;
  }

  @Override
  public Vec build() {
    return new SparseVec(index, indices.toArray(), data.toArray());
  }

  @Override
  public void clear() {
    data.clear();
    indices.clear();
  }
}
