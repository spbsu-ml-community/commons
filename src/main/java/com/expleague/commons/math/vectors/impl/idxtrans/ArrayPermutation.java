package com.expleague.commons.math.vectors.impl.idxtrans;

import com.expleague.commons.math.vectors.IndexTransformation;
import com.expleague.commons.seq.IntSeq;

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

  public ArrayPermutation(final int[] perm) {
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

  public ArrayPermutation(final IntSeq permSeq) {
    this.perm = new int[permSeq.length()];
    for (int i = 0; i < perm.length; i++) {
      perm[i] = permSeq.at(i);
      maxBack = max(perm[i], maxBack);
      minBack = min(perm[i], minBack);
    }
    backPerm = new int[maxBack + 1];
    Arrays.fill(backPerm, -1);
    for (int i = 0; i < perm.length; i++)
      backPerm[perm[i]] = i;
  }

  @Override
  public int forward(final int index) {
    return perm[index];
  }

  @Override
  public int backward(final int oldIndex) {
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
  public IndexTransformation apply(final IndexTransformation trans) {
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
