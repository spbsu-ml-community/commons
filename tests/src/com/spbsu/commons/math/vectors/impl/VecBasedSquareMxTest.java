package com.spbsu.commons.math.vectors.impl;

import junit.framework.TestCase;

/**
 * Created with IntelliJ IDEA.
 * ksen | 18:20 02.03.2013 | commons
 */

public class VecBasedSquareMxTest extends TestCase {

  private VecBasedSquareMx matrix = new VecBasedSquareMx();

  public void testConstructors() {
    matrix = new VecBasedSquareMx();
    assertTrue(matrix.columns() == 0 &
               matrix.rows() == 0 &
               matrix.dim() == 0);

    matrix = new VecBasedSquareMx(10);
    assertTrue(matrix.columns() == 10 &
               matrix.rows() == 10 &
               matrix.dim() == 10);

    matrix = new VecBasedSquareMx(new ArrayVec(1, 2, 3, 4));
    assertTrue(matrix.columns() == 2 &
               matrix.rows() == 2 &
               matrix.dim() == 2);
//    Exception. It's quite right.
//    matrix = new VecBasedSquareMx(new ArrayVec(1, 2, 3));
  }

  public void testGet() {
    build();
    for(int i = 0; i < 3; i++)
      for(int j = 0; j < 3; j++)
        assertTrue(matrix.get(i, j) == i + j);
    assertTrue(matrix.columns() == 3 &
               matrix.rows() == 3 &
               matrix.dim() == 3);
  }

  public void testSet() {
    build();
    assertTrue(matrix.columns() == 3 &
               matrix.rows() == 3 &
               matrix.dim() == 3);

    matrix = new VecBasedSquareMx(4);
    for(int i = 0; i < 3; i++)
      for(int j = 0; j < 3; j++)
        matrix.set(i, j, 0);
    assertTrue(matrix.columns() == 4 &
               matrix.rows() == 4 &
               matrix.dim() == 4);
    for(int i = 0; i < 4; i++)
      for(int j = 0; j < 4; j++)
        assertTrue(matrix.get(i, j) == 0);
  }

  public void testAdjust() {
    build();
    matrix.adjust(0, 0, 10);
    assertTrue(matrix.get(0, 0) == 10);
    matrix.adjust(10, 10, 5);
    assertTrue(matrix.get(10, 10) == 5);
    assertTrue(matrix.columns() == 11 &
            matrix.rows() == 11 &
            matrix.dim() == 11);
  }

  public void testVecGet() {
    build();
    assertTrue(matrix.get(0) == 0);
    assertTrue(matrix.get(1) == 1);
    assertTrue(matrix.get(2) == 2);
    assertTrue(matrix.get(3) == 1);
    assertTrue(matrix.get(4) == 2);
    assertTrue(matrix.get(5) == 3);
    assertTrue(matrix.get(6) == 4);
    assertTrue(matrix.get(7) == 3);
    assertTrue(matrix.get(8) == 2);
  }

  public void testVecSet() {
    matrix.set(0, 10);
    matrix.set(6, 0);
    matrix.set(12, 100);
    assertTrue(matrix.dim() == 4);
    assertTrue(matrix.get(0, 0) == 10);
    assertTrue(matrix.get(2, 2) == 0);
    assertTrue(matrix.get(3, 3) == 100);
    assertTrue(matrix.get(0, 3) == 0);
  }

  public void testVecAdjust() {
    matrix.adjust(0, 10);
    matrix.adjust(12, 5);
    matrix.set(2, 2, 11);
    assertTrue(matrix.dim() == 4);
    assertTrue(matrix.get(0) == 10);
    assertTrue(matrix.get(3, 3) == 5);
    assertTrue(matrix.get(6) == 11);
  }

  public void testToArray() {
    build();
    double[] test = matrix.toArray();
    assertTrue(test[0] == 0);
    assertTrue(test[1] == 1);
    assertTrue(test[2] == 2);
    assertTrue(test[3] == 1);
    assertTrue(test[4] == 2);
    assertTrue(test[5] == 3);
    assertTrue(test[6] == 4);
    assertTrue(test[7] == 3);
    assertTrue(test[8] == 2);
  }

  public void testSparse() {
    matrix = new VecBasedSquareMx(new ArrayVec());
    assertFalse(matrix.sparse());
    matrix = new VecBasedSquareMx();
    assertTrue(matrix.sparse());
  }

  private void build() {
    for(int i = 0; i < 3; i++)
      for(int j = 0; j < 3; j++)
        matrix.set(i, j, i + j);
  }

}
