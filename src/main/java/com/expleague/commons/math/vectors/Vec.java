package com.expleague.commons.math.vectors;

import com.expleague.commons.func.IntDoubleConsumer;
import com.expleague.commons.math.MathTools;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.seq.Seq;

import java.util.Arrays;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 13:10:48
 */
public interface Vec extends Seq<Double> {
  Vec EMPTY = new ArrayVec();

  double get(int i);
  Vec set(int i, double val);
  Vec adjust(int i, double increment);

  /** ordered access to non zero elements */
  VecIterator nonZeroes();
  /** unordered access to non zero elements */
  void visitNonZeroes(IntDoubleConsumer consumer);

  int dim();

  DoubleStream stream();
  double[] toArray();

  void toArray(final double[] src, final int offset);

  @Override
  Vec sub(int start, int len);

  abstract class Stub extends Seq.Stub<Double> implements Vec {
    @Override
    public final Double at(final int i) {
      return get(i);
    }

    @Override
    public final int length() {
      return Vec.Stub.this.dim();
    }

    @Override
    public Vec sub(int[] indices) {
      return new ArrayVec(IntStream.of(indices).mapToDouble(this::get).toArray());
    }

    @Override
    public final String toString() {
      return MathTools.CONVERSION.convert(this, CharSequence.class).toString();
    }

    @Override
    public final int hashCode() {
      return VecTools.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
      return obj instanceof Vec && VecTools.equals(this, (Vec)obj);
    }

    @Override
    public boolean isImmutable() {
      return false;
    }

    @Override
    public double[] toArray() {
      final double[] result = new double[dim()];
      final VecIterator nz = nonZeroes();
      while (nz.advance())
        result[nz.index()] = nz.value();
      return result;
    }

    @Override
    public void toArray(final double[] src, final int offset) {
      final VecIterator nz = nonZeroes();
      final int dim = dim();
      Arrays.fill(src, offset, offset + dim, 0.);
      while (nz.advance())
        src[offset + nz.index()] = nz.value();
    }

    @Override
    public Class<Double> elementType() {
      return double.class;
    }

    @Override
    public DoubleStream stream() {
      return IntStream.range(0, length()).mapToDouble(this::get);
    }

    @Override
    public void visitNonZeroes(IntDoubleConsumer consumer) {
      final VecIterator nz = nonZeroes();
      while (nz.advance()) {
        consumer.accept(nz.index(), nz.value());
      }
    }
  }
}

