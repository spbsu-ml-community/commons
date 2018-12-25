package com.expleague.commons.math.vectors.impl.vectors;

import com.expleague.commons.math.vectors.Basis;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecIterator;
import com.expleague.commons.math.vectors.impl.iterators.SparseVecNZIterator;
import com.expleague.commons.math.vectors.impl.idxtrans.SubVecTransformation;
import com.expleague.commons.util.ArrayTools;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 15:52:45
 */
public class CustomBasisVec<B extends Basis> extends Vec.Stub {
  public TIntArrayList indices = new TIntArrayList();
  public TDoubleArrayList values = new TDoubleArrayList();
  protected B basis;
  private ArrayVec backup;

  public CustomBasisVec(final B basis, final int[] indeces, final double[] values) {
    this.basis = basis;
    init(indeces, values);
  }

  public CustomBasisVec(final B basis) {
    this.basis = basis;
  }

  public CustomBasisVec(final B basis, int capacityHint) {
    indices = new TIntArrayList(capacityHint);
    values = new TDoubleArrayList(capacityHint);
    this.basis = basis;
  }

  protected CustomBasisVec() {
  }

  protected void init(final int[] indices, final double[] values) {
    if (!ArrayTools.isSorted(indices))
      ArrayTools.parallelSort(indices, values);
    this.indices.add(indices);
    this.values.add(values);
  }

  @Override
  public double get(final int i) {
    final int realIndex = index(i);
    if (realIndex >= 0 && indices.getQuick(realIndex) == i) {
      return values.getQuick(realIndex);
    }
    return 0;
  }

  @Override
  public Vec set(final int i, final double val) {
//    if (Double.isNaN(val))
//      throw new IllegalArgumentException();
    final int realIndex = index(i);
    if (realIndex >= 0 && indices.getQuick(realIndex) == i) {
      if (val == 0) {
        values.removeAt(realIndex);
        indices.removeAt(realIndex);
      }
      else
        values.setQuick(realIndex, val);
    }
    else if (val != 0) {
      indices.insert(-realIndex - 1, i);
      values.insert(-realIndex - 1, val);
    }

    return this;
  }

  private int index(final int n) {
    final TIntArrayList indicesLocal = indices;
    final int size = indicesLocal.size();
    if (size == 0 || n > indicesLocal.getQuick(size - 1))
      return -size-1;
    if (size < 16) {
      for (int i = 0; i < size; i++) { // jit just suck to insert SSE here
        final int idx = indicesLocal.getQuick(i);
        if (n <= idx)
          return n == idx ? i : -i-1;
      }
      return -size-1;
    }
    return indicesLocal.getQuick(size - 1) == n ? size - 1 : indicesLocal.binarySearch(n);
  }

  @Override
  public Vec adjust(final int i, final double increment) {
    if (Double.isNaN(increment))
      throw new IllegalArgumentException();
    final int realIndex = index(i);
    if (realIndex >= 0 && indices.getQuick(realIndex) == i) {
      final double newValue = values.getQuick(realIndex) + increment;
      if (newValue == 0) {
        values.removeAt(realIndex);
        indices.removeAt(realIndex);
      }
      else values.setQuick(realIndex, newValue);
    }
    else if (increment != 0) {
      indices.insert(-realIndex - 1, i);
      values.insert(-realIndex - 1, increment);
    }

    return this;
  }

  @Override
  public VecIterator nonZeroes() {
    return new SparseVecNZIterator(this);
  }
  
  public B basis() {
    return basis;
  }

  @Override
  public int dim() {
    return basis.size();
  }

  @Override
  public synchronized double[] toArray() {
    final double[] result = new double[basis.size()];
    toArray(result, 0);
    return result;
  }

  @Override
  public void toArray(final double[] src, final int offset) {
    Arrays.fill(src, offset, offset + dim(), 0);
    final int indicesLen = indices.size();
    for (int i = 0; i < indicesLen; ++i) {
      src[offset + indices.getQuick(i)] = values.getQuick(i);
    }
  }

  @Override
  public Vec sub(final int start, final int len) {
    final int end = start + len;
    int sindex = 0;
    int eindex = 0;
    for (int i = 0; i < indices.size() && indices.get(i) < end; i++) {
      if (indices.get(i) < start)
        sindex++;
      eindex++;
    }
    final int[] indices = new int[eindex - sindex];
    final double[] values = new double[eindex - sindex];
    for (int i = 0; i < indices.length; i++) {
      indices[i] = this.indices.get(i + sindex) - start;
      values[i] = this.values.get(i + sindex);
    }
    return new IndexTransVec(this, new SubVecTransformation(start, len));
  }


  @Override
  public boolean isImmutable() {
    return false;
  }

  /**way faster than set, but index must be greater all indices we already have in vector */
  public void add(final int index, final double v) {
    if (v != 0.) {
      this.indices.add(index);
      this.values.add(v);
    }
  }

  public void clear() {
    this.indices.clear();
    this.values.clear();
  }

  public int size() {
    return indices.size();
  }
}
