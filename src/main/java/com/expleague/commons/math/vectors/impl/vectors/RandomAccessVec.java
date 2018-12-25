package com.expleague.commons.math.vectors.impl.vectors;

import com.expleague.commons.func.IntDoubleConsumer;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;

import java.util.Arrays;

public class RandomAccessVec extends Vec.Stub {
  private final int dim;
  private final TIntDoubleMap data;

  public RandomAccessVec(int dim) {
    this.dim = dim;
    data = new TIntDoubleHashMap();
  }

  public RandomAccessVec(int dim, TIntDoubleMap data) {
    this.dim = dim;
    this.data = data;
  }

  public TIntDoubleMap data() {
    return data;
  }

  @Override
  public double get(int i) {
    return data.get(i);
  }

  @Override
  public Vec set(int i, double val) {
    if (val == 0)
      data.remove(i);
    else
      data.put(i, val);
    return this;
  }

  @Override
  public Vec adjust(int i, double increment) {
    if (data.adjustOrPutValue(i, increment, increment) == 0.0)
      data.remove(i);
    return this;
  }

  public int size() {
    return data.size();
  }

  @Override
  public VecIterator nonZeroes() {
    final int[] indices = data.keys();
    Arrays.sort(indices);
    return new VecIterator() {
      int index = 0;
      double val = Double.NaN;
      @Override
      public int index() {
        return index;
      }

      @Override
      public double value() {
        if (Double.isNaN(val)) {
          val = data.get(index);
        }
        return val;
      }

      @Override
      public boolean isValid() {
        return index >= 0 && index < indices.length;
      }

      @Override
      public boolean advance() {
        index++;
        return isValid();
      }

      @Override
      public boolean seek(int pos) {
        index = Arrays.binarySearch(indices, pos);
        return index >= 0;
      }

      @Override
      public double setValue(double v) {
        if (v == 0)
          return data.remove(index);
        return data.put(index, v);
      }
    };
  }

  @Override
  public void visitNonZeroes(IntDoubleConsumer consumer) {
    data.forEachEntry((i, v) -> {
      consumer.accept(i, v);
      return true;
    });
  }

  @Override
  public int dim() {
    return dim;
  }

  @Override
  public Vec sub(int start, int len) {
    final RandomAccessVec result = new RandomAccessVec(dim);
    final int end = start + len;
    data.forEachEntry((k, v) -> {
      if (k >= start && k < end)
        result.set(k, v);
      return true;
    });
    return result;
  }

  public void clear() {
    data.clear();
  }
}
