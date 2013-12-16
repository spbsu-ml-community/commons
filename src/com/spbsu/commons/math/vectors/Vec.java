package com.spbsu.commons.math.vectors;

import com.spbsu.commons.util.ArrayPart;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 13:10:48
 */
public interface Vec {
  double get(int i);
  Vec set(int i, double val);
  Vec adjust(int i, double increment);
  VecIterator nonZeroes();

  int dim();

  double[] toArray();

  Vec sub(int start, int len);
}

