package com.spbsu.commons.math.vectors.impl.vectors;

import com.spbsu.commons.math.vectors.BasisVecIterator;
import com.spbsu.commons.math.vectors.GenericBasis;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.impl.iterators.ObjectBasisVecIterator;
import com.spbsu.commons.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 18:29:22
 */
public class CommonBasisVec<T> extends CustomBasisVec<GenericBasis<T>> {
  public CommonBasisVec(GenericBasis<T> basis, T[] indeces, double[] values) {
    super(basis, transform(basis, indeces), values);
  }

  public CommonBasisVec(GenericBasis<T> basis) {
    super(basis);
  }

  private static <T> int[] transform(GenericBasis<T> basis, T[] indeces) {
    final int[] iindeces = new int[indeces.length];
    for (int i = 0; i < indeces.length; i++) {
      iindeces[i] = basis.toIndex(indeces[i]);
    }
    return iindeces;
  }

  public double get(T key) {
    return get(basis().toIndex(key));
  }

  public Vec set(T key, double val) {
    return super.set(basis().toIndex(key), val);
  }

  public Vec adjust(T key, double increment) {
    return super.adjust(basis().toIndex(key), increment);
  }

  public BasisVecIterator<T> iterator() {
    return new ObjectBasisVecIterator<T>(nonZeroes(), basis);
  }

  public GenericBasis<T> basis() {
    return basis;
  }
}
