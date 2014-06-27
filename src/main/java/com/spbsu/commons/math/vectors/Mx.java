package com.spbsu.commons.math.vectors;

/**
 * User: solar
 * Date: 26.07.12
 * Time: 20:52
 */
public interface Mx extends Vec {
  double get(int i, int j);
  Mx set(int i, int j, double val);
  Mx adjust(int i, int j, double increment);

  Mx sub(int i, int j, int height, int width);
  Vec row(int i);
  Vec col(int j);

  MxIterator nonZeroes();
  MxBasis basis();

  int columns();
  int rows();
}
