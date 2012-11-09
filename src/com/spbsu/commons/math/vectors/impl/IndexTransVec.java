package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.*;
import com.spbsu.commons.math.vectors.impl.iterators.TransformedArrayVecNZIterator;
import com.spbsu.commons.math.vectors.impl.iterators.TransformedSparseVecIterator;
import com.spbsu.commons.util.ArrayTools;
import gnu.trove.TIntArrayList;

/**
 * User: solar
 * Date: 9/14/12
 * Time: 1:17 PM
 */
public class IndexTransVec implements Vec {
  private final Vec base;
  private final IndexTransformation transformation;
  private final Basis basis;

  public IndexTransVec(Vec base, IndexTransformation transformation, Basis basis) {
    if (base instanceof IndexTransVec) {
      final IndexTransVec transVec = (IndexTransVec) base;
      this.base = transVec.base;
      this.transformation = transVec.transformation.apply(transformation);
      this.basis = basis;
    }
    else {
      this.base = base;
      this.transformation = transformation;
      this.basis = basis;
    }
  }

  public double get(int i) {
    return base.get(transformation.forward(i));
  }

  public Vec set(int i, double val) {
    return base.set(transformation.forward(i), val);
  }

  public Vec adjust(int i, double increment) {
    return base.adjust(transformation.forward(i), increment);
  }

  public VecIterator nonZeroes() {
    if (base instanceof SparseVec) {
      SparseVec sparseVec = (SparseVec)base;
      TIntArrayList indices = sparseVec.indices;

      final TIntArrayList nzIndices = new TIntArrayList(indices.size());
      final TIntArrayList transformed = new TIntArrayList(indices.size());
      int end = transformation.oldIndexEndHint();
      int firstRelevant = indices.binarySearch(transformation.oldIndexStartHint());
      firstRelevant = firstRelevant >= 0 ? firstRelevant : -firstRelevant - 1;
      for (int i = firstRelevant; i < indices.size(); i++) {
        int index = indices.getQuick(i);
        if (index > end)
          break;
        int newIndex = transformation.backward(index);
        if (newIndex >= 0) {
          nzIndices.add(newIndex);
          transformed.add(i);
        }
      }
      int[] transA = transformed.toNativeArray();
      int[] nzIndicesA = nzIndices.toNativeArray();
      ArrayTools.parallelSort(nzIndicesA, transA);
      return new TransformedSparseVecIterator(indices, sparseVec.values, new TIntArrayList(nzIndicesA), new TIntArrayList(transA));
    }
    else if (base instanceof ArrayVec) {
      return new TransformedArrayVecNZIterator(((ArrayVec)base).values, transformation);
    }
    else throw new IllegalArgumentException("Can not produce NZ itarator for base type " + base.getClass().toString());
  }

  public Basis basis() {
    return basis;
  }

  public int dim() {
    return basis.size();
  }

  @Override
  public double[] toArray() {
    double[] result = new double[basis.size()];
    VecIterator iter = nonZeroes();
    while (iter.advance())
      result[iter.index()] = iter.value();
    return result;
  }

  @Override
  public boolean sparse() {
    return base.sparse();
  }
}
