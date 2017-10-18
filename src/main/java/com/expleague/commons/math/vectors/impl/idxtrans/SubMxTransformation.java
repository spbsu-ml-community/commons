package com.expleague.commons.math.vectors.impl.idxtrans;

import com.expleague.commons.math.vectors.IndexTransformation;

/**
 * User: solar
 * Date: 10/10/12
 * Time: 9:49 PM
 */
public class SubMxTransformation implements IndexTransformation {
  public final int start;
  public final int columns;
  public final int i;
  public final int j;
  public final int width;
  public final int height;

  public SubMxTransformation(final int columns, final int i, final int j, final int height, final int width) {
    this.start = i * columns + j;
    this.columns = columns;
    this.i = i;
    this.j = j;
    this.width = width;
    this.height = height;
  }

  @Override
  public int forward(final int newIndex) {
    final int x = newIndex / width;
    final int y = newIndex % width;
    return start + y + x * columns;
  }

  @Override
  public int backward(final int oldIndex) {
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
  public IndexTransformation apply(final IndexTransformation trans) {
    if (trans instanceof SubMxTransformation) {
      SubMxTransformation other = (SubMxTransformation) trans;
      return new SubMxTransformation(other.columns, i + other.i, j + other.j, height, width);
    }
    return new CompositeTransformation(new IndexTransformation[]{this, trans});
  }
}
