package com.expleague.commons.math.vectors.impl.vectors;

import com.expleague.commons.math.vectors.IndexTransformation;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecIterator;
import com.expleague.commons.math.vectors.impl.idxtrans.SubVecTransformation;
import com.expleague.commons.math.vectors.impl.iterators.MxIteratorImpl;
import com.expleague.commons.math.vectors.impl.iterators.SkipVecNZIterator;
import com.expleague.commons.math.vectors.impl.iterators.TransformedSparseVecIterator;
import com.expleague.commons.math.vectors.impl.mx.ColsVecArrayMx;
import com.expleague.commons.math.vectors.impl.mx.RowsVecArrayMx;
import com.expleague.commons.math.vectors.impl.mx.VecBasedMx;
import com.expleague.commons.util.ArrayTools;
import gnu.trove.list.array.TIntArrayList;

/**
 * User: solar
 * Date: 9/14/12
 * Time: 1:17 PM
 */
public class IndexTransVec extends Vec.Stub {
  private final Vec base;
  private final IndexTransformation transformation;

  public IndexTransVec(Vec base, final IndexTransformation transformation) {
    if (base instanceof VecBasedMx) {
      base = ((VecBasedMx) base).vec;
    }
    if (base instanceof IndexTransVec) {
      final IndexTransVec transVec = (IndexTransVec) base;
      this.base = transVec.base;
      this.transformation = transformation.apply(transVec.transformation);
    }
    else {
      this.base = base;
      this.transformation = transformation;
    }
  }

  @Override
  public double get(final int i) {
    return base.get(transformation.forward(i));
  }

  @Override
  public Vec set(final int i, final double val) {
    return base.set(transformation.forward(i), val);
  }

  @Override
  public Vec adjust(final int i, final double increment) {
    return base.adjust(transformation.forward(i), increment);
  }

  @Override
  public VecIterator nonZeroes() {
    Vec base = this.base;
    if (this.base instanceof VecBasedMx) {
      base = ((VecBasedMx)this.base).vec;
    }
    final VecIterator result;
    if (base instanceof SparseVec) {
      final SparseVec sparseVec = (SparseVec)base;
      final TIntArrayList indices = sparseVec.indices;

      final TIntArrayList nzIndices = new TIntArrayList(indices.size());
      final TIntArrayList transformed = new TIntArrayList(indices.size());
      final int end = transformation.oldIndexEndHint();
      int firstRelevant = indices.binarySearch(transformation.oldIndexStartHint());
      firstRelevant = firstRelevant >= 0 ? firstRelevant : -firstRelevant - 1;
      for (int i = firstRelevant; i < indices.size(); i++) {
        final int index = indices.getQuick(i);
        if (index > end)
          break;
        final int newIndex = transformation.backward(index);
        if (newIndex >= 0) {
          nzIndices.add(newIndex);
          transformed.add(i);
        }
      }
      final int[] transA = transformed.toArray();
      final int[] nzIndicesA = nzIndices.toArray();
      ArrayTools.parallelSort(nzIndicesA, transA);
      result = new TransformedSparseVecIterator(indices, sparseVec.values, new TIntArrayList(nzIndicesA), new TIntArrayList(transA));
    }
    else if (base instanceof Vec) {
      result = new SkipVecNZIterator(this);
    }
    else if (base instanceof ArrayVec) {
      result = new SkipVecNZIterator(this);
    }
    else if (base instanceof ColsVecArrayMx) {
      result = new SkipVecNZIterator(this);
    }
    else if (base instanceof RowsVecArrayMx) {
      result = new SkipVecNZIterator(this);
    } else
      throw new IllegalArgumentException("Can not produce NZ itarator for base type " + base.getClass().toString());
    return this.base instanceof VecBasedMx ? new MxIteratorImpl(result, ((VecBasedMx) this.base).columns()) : result;
  }

  @Override
  public int dim() {
    return transformation.newDim();
  }

  @Override
  public double[] toArray() {
    final double[] result = new double[transformation.newDim()];
    if (base instanceof SparseVec) {
      final VecIterator iter = base.nonZeroes();
      while (iter.advance()) {
        final int newIndex = transformation.backward(iter.index());
        if (newIndex >= 0)
          result[newIndex] = iter.value();
      }
    }
    else {
      for (int i = 0; i < result.length; i++) {
        result[i] = base.get(transformation.forward(i));
      }
    }
    return result;
  }

  @Override
  public Vec sub(final int start, final int len) {
    return new IndexTransVec(this, new SubVecTransformation(start, len));
  }

  @Override
  public boolean isImmutable() {
    return false;
  }
}
