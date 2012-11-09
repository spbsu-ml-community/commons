package com.spbsu.commons.math.vectors.impl.idxtrans;

import com.spbsu.commons.math.vectors.IndexTransformation;

import java.util.Arrays;

/**
 * User: solar
 * Date: 10/9/12
 * Time: 7:16 AM
 */
public class RowsPermutation implements IndexTransformation {
  private final int[] perm;
  private final int[] permBack;
  private final int columns;

  public RowsPermutation(int[] perm, int columns) {
    this.perm = perm;
    permBack = new int[perm.length];
    Arrays.fill(permBack, -1);
    for (int i = 0; i < permBack.length; i++)
      permBack[perm[i]] = i;
    this.columns = columns;
  }

  @Override
  public int forward(int index) {
    return perm[index/columns] + index % columns;
  }

  @Override
  public int backward(int oldIndex) {
    return permBack[oldIndex];
  }

  @Override
  public int newDim() {
    return permBack.length;
  }

  @Override
  public int oldIndexStartHint() {
    return 0;
  }

  @Override
  public int oldIndexEndHint() {
    return perm.length * columns;
  }

  @Override
  public IndexTransformation apply(IndexTransformation trans) {
    final IndexTransformation[] sequence = new IndexTransformation[]{
            this, trans
    };
    return new CompositeTransformation(sequence);
  }

}
