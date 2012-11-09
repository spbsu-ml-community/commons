package com.spbsu.commons.math.vectors.impl.idxtrans;

import com.spbsu.commons.math.vectors.IndexTransformation;

/**
 * User: solar
 * Date: 10/10/12
 * Time: 9:49 PM
 */
public class SubMxTransformation implements IndexTransformation {
  final int start;
  final int columns;
  final int i;
  final int j;
  final int width;
  final int height;

  public SubMxTransformation(int columns, int i, int j, int width, int height) {
    this.start = i * columns + j;
    this.columns = columns;
    this.i = i;
    this.j = j;
    this.width = width;
    this.height = height;
  }

  @Override
  public int forward(int newIndex) {
    final int x = newIndex / width;
    final int y = newIndex % width;
    return start + y + x * columns;
  }

  @Override
  public int backward(int oldIndex) {
    if (oldIndex < start)
      return -1;
    final int x = oldIndex / columns;
    if (x < i || x >= i + height) return -1;
    final int y = oldIndex % columns;
    if (y < j || y >= j + width) return -1;
    return (x - i) * width + y - j;
  }

  @Override
  public int newDim() {
    return height * width;
  }

  @Override
  public int oldIndexStartHint() {
    return start;
  }

  @Override
  public int oldIndexEndHint() {
    return start + columns * (height - 1) + j + width;
  }

  @Override
  public IndexTransformation apply(IndexTransformation trans) {
    return new CompositeTransformation(new IndexTransformation[]{this, trans});
  }
}
