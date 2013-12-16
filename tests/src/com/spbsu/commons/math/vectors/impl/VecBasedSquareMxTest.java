package com.spbsu.commons.math.vectors.impl;

import com.spbsu.commons.math.vectors.*;
import com.spbsu.commons.util.ArrayPart;
import junit.framework.TestCase;

/**
* Created with IntelliJ IDEA.
* ksen | 18:20 02.03.2013 | commons
*/

public class VecBasedSquareMxTest extends TestCase {

  private MapBasis<String> basis = new MapBasis<String>();
  private VecBasedSquareMx<String> matrix = new VecBasedSquareMx<String>(basis);

  public void testGet() {
    build();
    System.out.println(matrix);
    for(int i = 0; i < 3; i++)
      for(int j = 0; j < 3; j++)
        assertTrue(matrix.get(i, j) == i * 3 + j);
    assertTrue(matrix.columns() == 3 &
            matrix.rows() == 3 &
            matrix.dim() == 3);
  }

  public void testSet() {
    build();
    assertTrue(matrix.columns() == 3 &
            matrix.rows() == 3 &
            matrix.dim() == 3);

    for(int i = 0; i < 4; i++)
      for(int j = 0; j < 4; j++)
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
    assertTrue(matrix.get(3) == 3);
    assertTrue(matrix.get(4) == 4);
    assertTrue(matrix.get(5) == 5);
    assertTrue(matrix.get(6) == 6);
    assertTrue(matrix.get(7) == 7);
    assertTrue(matrix.get(8) == 8);
  }

  public void testToArray() {
    build();
    double[] test = matrix.toArray();
    assertTrue(test[0] == 0);
    assertTrue(test[1] == 1);
    assertTrue(test[2] == 2);
    assertTrue(test[3] == 3);
    assertTrue(test[4] == 4);
    assertTrue(test[5] == 5);
    assertTrue(test[6] == 6);
    assertTrue(test[7] == 7);
    assertTrue(test[8] == 8);
  }

  public void testNonZeroes() {
    matrix.set(0, 0, 0);
    matrix.set(0, 1, 1);
    matrix.set(1, 0, 0);
    matrix.set(1, 1, 2);
    MxIterator iterator = matrix.nonZeroes();
    iterator.advance();
    assertTrue(iterator.value() == 1);
    iterator.advance();
    assertTrue(iterator.value() == 2);
    assertTrue(!iterator.advance());
  }

  public void testCol() {
    build();
    Vec test = matrix.col(0);
    assertTrue(test.get(0) == 0);
    assertTrue(test.get(1) == 3);
    assertTrue(test.get(2) == 6);
  }

  public void testRow() {
    build();
    Vec test = matrix.row(2);
    assertTrue(test.get(0) == 6);
    assertTrue(test.get(1) == 7);
    assertTrue(test.get(2) == 8);
  }

  public void testSub() {
    build();
    Mx test = matrix.sub(1, 1, 2, 2);
    assertTrue(test.get(0, 0) == 4);
    assertTrue(test.get(0, 1) == 5);
    assertTrue(test.get(1, 0) == 7);
    assertTrue(test.get(1, 1) == 8);
    assertTrue(test.columns() == 2);
    assertTrue(test.rows() == 2);
  }

  public void testGeneric() {
    GenericBasis<String> basis = new MapBasis<String>();
    basis.add("A");
    basis.add("B");
    basis.add("C");
    basis.add("D");
    basis.add("E");
    VecBasedSquareMx<String> gMatrix = new VecBasedSquareMx<String>(basis);
    gMatrix.set("A", "A", 3);
    gMatrix.set("C", "B", 4);

    basis.add("F");

    gMatrix.set("F", "F", 31);
    assertTrue(gMatrix.get("A", "A") == 3);
    assertTrue(gMatrix.get("C", "B") == 4);
    assertTrue(gMatrix.get("F", "F") == 31);
    gMatrix.adjust("F", "F", -31);
    assertTrue(gMatrix.get("F", "F") == 0);
  }

  private void build() {
    for(int i = 0; i < 3; i++) {
      basis.add("" + i);
      for(int j = 0; j < 3; j++)
        matrix.set(i, j, i * 3 + j);
    }
  }

}
