package com.spbsu.commons.math.vectors;

/**
 * User: solar
 * Date: 9/14/12
 * Time: 1:12 PM
 */
public interface IndexTransformation {
  int forward(int newIndex);
  int backward(int oldIndex);

  int newDim();

  int oldIndexStartHint();
  int oldIndexEndHint();

  IndexTransformation apply(IndexTransformation trans);
}
