package com.expleague.commons.math.vectors;

/**
 * Created by hrundelb on 30.10.17.
 */
public interface OperableVec<T extends OperableVec> {

  void add(T other);

  double mul(T other);

  void fill(double val);

  void inscale(T other, double scale);

  void scale(double scale);

  double sum();

  double sum2();
}
