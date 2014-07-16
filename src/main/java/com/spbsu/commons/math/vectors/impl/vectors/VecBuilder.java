package com.spbsu.commons.math.vectors.impl.vectors;

import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.impl.iterators.SkipVecNZIterator;
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
    append((double)aDouble);
    return this;
  }

  public void append(double d) {
    data.add(d);
  }

  @Override
  public Vec build() {
    return new ArrayVec(data.toArray());
  }
}
