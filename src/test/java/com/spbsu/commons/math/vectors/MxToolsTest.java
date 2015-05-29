package com.spbsu.commons.math.vectors;

import com.spbsu.commons.math.vectors.impl.mx.VecBasedMx;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import junit.framework.TestCase;

public class MxToolsTest extends TestCase {

  public void testSolveSystemLq() throws Exception {
    final Mx A = new VecBasedMx(2, new ArrayVec(
        1, 2,
        1, 5
    ));
    final Vec b = new ArrayVec(5, 11);
    final Vec x = MxTools.solveSystemLq(A, b);
    assertTrue(VecTools.distance(MxTools.multiply(A, x), b) < 1e-4);
  }
  //Todo: find bug in LQ. Test below
  public void FailedtestHouseHolderLQ()
  {
    final Mx a = new VecBasedMx(4, new ArrayVec(
        2, 0.25, 0, 0,
        0.25, 0.0625, 0, 0,
        0, 0, 2, 1.25,
        0, 0, 1.25, 0.8125
    ));
    Mx l = new VecBasedMx(4, 4);
    Mx q = new VecBasedMx(4, 4);
    MxTools.householderLQ(a, l, q);
    assertEquals(0, VecTools.distance(a, MxTools.multiply(l, q)), 1e-3);
  }

  public void testCholesky()
  {
    final Mx a = new VecBasedMx(4, new ArrayVec(
        2, 0.25, 0, 0,
        0.25, 0.0625, 0, 0,
        0, 0, 2, 1.25,
        0, 0, 1.25, 0.8125
    ));
    Mx l = MxTools.choleskyDecomposition(a);
    assertEquals(0, VecTools.distance(a, MxTools.multiply(l, MxTools.transpose(l))), 1e-3);
  }

  public void testCholeskySolve()
  {
    final Mx a = new VecBasedMx(4, new ArrayVec(
        2, 0.25, 0, 0,
        0.25, 0.0625, 0, 0,
        0, 0, 2, 1.25,
        0, 0, 1.25, 0.8125
    ));
    final Vec b = new ArrayVec(2, 0.5, 402, 251.5);
    Vec answer = MxTools.solveCholesky(a, b);
    assertEquals(0, VecTools.distance(b, MxTools.multiply(a, answer)), 1e-3);
  }

  public void testIterativeSolve() {
    final Mx a = new VecBasedMx(4, new ArrayVec(
        2, 0.25, 0, 0,
        0.25, 0.0625, 0, 0,
        0, 0, 2, 1.25,
        0, 0, 1.25, 0.8125
    ));
    final Vec b = new ArrayVec(2, 0.5, 402, 251.5);
    Vec answer = MxTools.solveGaussZeildel(a, b);
    assertEquals(0, VecTools.distance(b, MxTools.multiply(a, answer)), 1e-3);
  }
}