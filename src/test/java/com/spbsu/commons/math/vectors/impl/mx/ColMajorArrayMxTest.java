package com.spbsu.commons.math.vectors.impl.mx;

import com.spbsu.commons.math.vectors.Mx;
import com.spbsu.commons.math.vectors.MxIterator;
import com.spbsu.commons.math.vectors.Vec;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * jmll
 * ksen
 * 14.April.2015 at 23:09
 */
public class ColMajorArrayMxTest extends TestCase {

  private static final double EPS = 1e-9;

  public void testCreate() {
    final Mx A = new ColMajorArrayMx(3, 4);

    assertEquals(3, A.rows());
    assertEquals(4, A.columns());

    final Mx B = new ColMajorArrayMx(10, 10);

    assertEquals(10, B.rows());
    assertEquals(10, B.columns());

    final Mx C = new ColMajorArrayMx(11, new double[33]);

    assertEquals(11, C.rows());
    assertEquals(3, C.columns());
  }

  public void testGetSet() throws Exception {
    final Mx A = new ColMajorArrayMx(3, 4);

    for (int i = 0; i < A.rows(); i++) {
      for (int j = 0; j < A.columns(); j++) {
        A.set(i, j, i * 31 + j);
      }
    }

    for (int i = 0; i < A.rows(); i++) {
      for (int j = 0; j < A.columns(); j++) {
        assertEquals(i * 31 + j, A.get(i, j), EPS);
      }
    }
  }

  public void testAdjust() throws Exception {
    final Mx A = new ColMajorArrayMx(3, 4);

    for (int i = 0; i < A.rows(); i++) {
      for (int j = 0; j < A.columns(); j++) {
        A.adjust(i, j, 100000000000.00000000001);
      }
    }

    for (int i = 0; i < A.rows(); i++) {
      for (int j = 0; j < A.columns(); j++) {
        assertEquals(100000000000.00000000001, A.get(i, j), EPS);
      }
    }
  }

  public void testToArray() throws Exception {
    final double[] data = {
         1,  2,  3,
         4,  5,  6,
         7,  8,  9,
        10, 11, 12
    };

    final Mx A = new ColMajorArrayMx(3, data);

    assertTrue(Arrays.equals(data, A.toArray()));

    final Mx B = new ColMajorArrayMx(3, 4);
    for (int j = 0; j < A.columns(); j++) {
      for (int i = 0; i < A.rows(); i++) {
        B.set(i, j, (i + 1) + j * 3);
      }
    }

    assertTrue(Arrays.equals(data, B.toArray()));
  }

  public void testSub() throws Exception {
    final Mx A = new ColMajorArrayMx(10, 11);

    for (int i = 0; i < A.rows(); i++) {
      for (int j = 0; j < A.columns(); j++) {
        A.set(i, j, i * 31 + j);
      }
    }

    final Mx B = A.sub(1, 2, 7, 9);

    for (int i = 0; i < B.rows(); i++) {
      for (int j = 0; j < B.columns(); j++) {
        assertEquals((i + 1) * 31 + (j + 2), B.get(i, j), EPS);
      }
    }
  }

  public void testRow() throws Exception {
    final Mx A = new ColMajorArrayMx(10, 11);

    for (int i = 0; i < A.rows(); i++) {
      for (int j = 0; j < A.columns(); j++) {
        A.set(i, j, i * 31 + j);
      }
    }

    final Vec b = A.row(3);

    for (int i = 0; i < b.dim(); i++) {
      assertEquals(3 * 31 + i, b.get(i), EPS);
    }
  }

  public void testColumn() throws Exception {
    final Mx A = new ColMajorArrayMx(10, 11);

    for (int i = 0; i < A.rows(); i++) {
      for (int j = 0; j < A.columns(); j++) {
        A.set(i, j, i * 31 + j);
      }
    }

    final Vec b = A.col(5);

    for (int i = 0; i < b.dim(); i++) {
      assertEquals(i * 31 + 5, b.get(i), EPS);
    }
  }

  public void testIterator() throws Exception {
    final double[] data = {
        0, 1, 1, 1,
        0, 0, 0, 0,
        1, 1, 1, 1,
        1, 0, 0, 0
    };
    final Mx A = new ColMajorArrayMx(4, data);

    final MxIterator iterator = A.nonZeroes();

    assertTrue(iterator.advance());
    assertTrue(0 == iterator.row() && 2 == iterator.column());
    assertTrue(8 == iterator.index());

    assertTrue(iterator.advance());
    assertTrue(0 == iterator.row() && 3 == iterator.column());
    assertTrue(12 == iterator.index());

    assertTrue(iterator.advance());
    assertTrue(1 == iterator.row() && 0 == iterator.column());
    assertTrue(1 == iterator.index());

    assertTrue(iterator.advance());
    assertTrue(1 == iterator.row() && 2 == iterator.column());
    assertTrue(9 == iterator.index());

    assertTrue(iterator.advance());
    assertTrue(2 == iterator.row() && 0 == iterator.column());
    assertTrue(2 == iterator.index());

    assertTrue(iterator.advance());
    assertTrue(2 == iterator.row() && 2 == iterator.column());
    assertTrue(10 == iterator.index());

    assertTrue(iterator.advance());
    assertTrue(3 == iterator.row() && 0 == iterator.column());
    assertTrue(3 == iterator.index());

    assertTrue(iterator.advance());
    assertTrue(3 == iterator.row() && 2 == iterator.column());
    assertTrue(11 == iterator.index());

    assertFalse(iterator.advance());
  }

}
