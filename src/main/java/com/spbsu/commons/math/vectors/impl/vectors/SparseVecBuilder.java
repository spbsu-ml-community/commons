package com.spbsu.commons.math.vectors.impl.vectors;

import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.seq.Seq;
import com.spbsu.commons.seq.SeqBuilder;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * User: solar
 * Date: 07.07.14
 * Time: 20:24
 */
public class SparseVecBuilder implements SeqBuilder<Double> {
  private final TDoubleArrayList data;
  private final TIntArrayList indices;
  int index = 0;

  public SparseVecBuilder() {
    data = new TDoubleArrayList();
    indices = new TIntArrayList();
  }

  public SparseVecBuilder(final int capacity) {
    data = new TDoubleArrayList(capacity);
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
