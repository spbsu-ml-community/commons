package com.spbsu.commons.math.vectors.impl.vectors;

import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.impl.iterators.SkipVecNZIterator;
import com.spbsu.commons.seq.GrowingSeq;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * User: solar
 * Date: 07.07.14
 * Time: 20:24
 */
public class VecBuilder extends Vec.Stub implements GrowingSeq<Double> {
  private final TDoubleArrayList data;

  public VecBuilder() {
    data = new TDoubleArrayList();
  }

  public VecBuilder(final int capacity) {
    data = new TDoubleArrayList(capacity);
  }

  @Override
  public double get(final int i) {
    return data.getQuick(i);
  }

  @Override
  public Vec set(final int i, final double val) {
    data.setQuick(i, val);
    return this;
  }

  @Override
  public Vec adjust(final int i, final double increment) {
    data.set(i, data.get(i) + increment);
    return this;
  }

  @Override
  public VecIterator nonZeroes() {
    return new SkipVecNZIterator(this);
  }

  @Override
  public int dim() {
    return data.size();
  }

  @Override
  public double[] toArray() {
    return data.toArray();
  }

  @Override
  public Vec sub(final int start, final int end) {
    return new ArrayVec(data.toArray());
  }

  @Override
  public boolean isImmutable() {
    return false;
  }

  @Override
  public GrowingSeq<Double> add(final Double val) {
    return append(val);
  }

  public GrowingSeq<Double> append(final double val) {
    data.add(val);
    return this;
  }
}
