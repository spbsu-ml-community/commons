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
public class ArrayPermutation implements IndexTransformation {
  private final int[] perm;
  private final int[] backPerm;
  int maxBack = 0;
  int minBack = 0;

  public ArrayPermutation(int[] perm) {
    this.perm = perm;
    for (int i = 0; i < perm.length; i++) {
      maxBack = max(perm[i], maxBack);
      minBack = min(perm[i], minBack);
    }
    backPerm = new int[maxBack + 1];
    Arrays.fill(backPerm, -1);
    for (int i = 0; i < perm.length; i++)
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
    return perm.length;
  }

  @Override
  public int oldIndexStartHint() {
    return minBack;
  }

  @Override
  public int oldIndexEndHint() {
    return maxBack;
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
