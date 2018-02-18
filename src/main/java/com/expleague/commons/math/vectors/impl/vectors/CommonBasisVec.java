package com.expleague.commons.math.vectors.impl.vectors;

import com.expleague.commons.math.vectors.BasisVecIterator;
import com.expleague.commons.math.vectors.GenericBasis;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.iterators.ObjectBasisVecIterator;
import org.jetbrains.annotations.NotNull;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 18:29:22
 */
public class CommonBasisVec<T> extends CustomBasisVec<GenericBasis<T>> {
  public CommonBasisVec(final GenericBasis<T> basis, final T[] indeces, final double[] values) {
    super(basis, transform(basis, indeces), values);
  }

  public CommonBasisVec(final GenericBasis<T> basis) {
    super(basis);
  }

  private static <T> int[] transform(final GenericBasis<T> basis, final T[] indeces) {
    final int[] iindeces = new int[indeces.length];
    for (int i = 0; i < indeces.length; i++) {
      iindeces[i] = basis.toIndex(indeces[i]);
    }
    return iindeces;
  }

  public double get(final T key) {
    return get(basis().toIndex(key));
  }

  public Vec set(final T key, final double val) {
    return super.set(basis().toIndex(key), val);
  }

  public Vec adjust(final T key, final double increment) {
    return super.adjust(basis().toIndex(key), increment);
  }

  @NotNull
  public BasisVecIterator<T> iterator() {
    return new ObjectBasisVecIterator<T>(nonZeroes(), basis);
  }

  @Override
  public GenericBasis<T> basis() {
    return basis;
  }
}
