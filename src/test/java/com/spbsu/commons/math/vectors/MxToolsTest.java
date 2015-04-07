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
}