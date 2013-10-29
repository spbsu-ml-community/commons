package com.spbsu.commons.math.vectors.impl.idxtrans;

import com.spbsu.commons.math.vectors.IndexTransformation;

import java.util.Arrays;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * User: solar
 * Date: 10/9/12
 * Time: 7:16 AM
 */
public class RowsPermutation implements IndexTransformation {
  private final int[] perm;
  private final int[] permBack;
  private final int columns;
  int maxBack = 0;
  int minBack = 0;


  public RowsPermutation(int[] perm, int columns) {
    this.perm = perm;
    for (int i = 0; i < perm.length; i++) {
      maxBack = max(perm[i], maxBack);
      minBack = min(perm[i], minBack);
    }
    permBack = new int[maxBack + 1];
    Arrays.fill(permBack, -1);
    for (int i = 0; i < perm.length; i++)
      permBack[perm[i]] = i;
    this.columns = columns;
  }

  @Override
  public int forward(int index) {
    if (index/columns >= perm.length)
      System.out.println();
    return perm[index/columns] * columns + index % columns;
  }

  @Override
  public int backward(int oldIndex) {
    return permBack[oldIndex];
  }

  @Override
  public int newDim() {
    return perm.length;
  }

  @Override
  public int oldIndexStartHint() {
    return minBack * columns;
  }

  @Override
  public int oldIndexEndHint() {
    return maxBack * columns;
  }

  @Override
  public IndexTransformation apply(IndexTransformation trans) {
    final IndexTransformation[] sequence = new IndexTransformation[]{
            this, trans
    };
    return new CompositeTransformation(sequence);
  }

}
