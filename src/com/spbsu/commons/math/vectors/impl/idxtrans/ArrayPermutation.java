package com.spbsu.commons.math.vectors.impl.idxtrans;

import com.spbsu.commons.math.vectors.IndexTransformation;

import java.util.Arrays;

/**
 * User: solar
 * Date: 10/9/12
 * Time: 7:16 AM
 */
public class ArrayPermutation implements IndexTransformation {
  private final int[] perm;
  private final int[] backPerm;

  public ArrayPermutation(int[] perm) {
    this.perm = perm;
    backPerm = new int[perm.length];
    Arrays.fill(backPerm, -1);
    for (int i = 0; i < backPerm.length; i++)
      backPerm[perm[i]] = i;
  }

  @Override
  public int forward(int index) {
    return perm[index];
  }

  @Override
  public int backward(int oldIndex) {
    return backPerm[oldIndex];
  }

  @Override
  public int newDim() {
    return backPerm.length;
  }

  @Override
  public int oldIndexStartHint() {
    return 0;
  }

  @Override
  public int oldIndexEndHint() {
    return perm.length;
  }

  @Override
  public IndexTransformation apply(IndexTransformation trans) {
    final IndexTransformation[] sequence = new IndexTransformation[]{
            this, trans
    };
    return new CompositeTransformation(sequence);
  }

  public int[] direct() {
    return perm;
  }

  public int[] reverse() {
    return backPerm;
  }
}
