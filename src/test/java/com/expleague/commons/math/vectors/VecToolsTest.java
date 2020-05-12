package com.expleague.commons.math.vectors;

import com.expleague.commons.math.MathTools;
import com.expleague.commons.math.stat.StatTools;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.math.vectors.impl.vectors.SparseVec;
import com.expleague.commons.random.FastRandom;
import junit.framework.TestCase;

/**
 * User: qdeee
 * Date: 30.01.15
 */
public class VecToolsTest extends TestCase {
  public void testDistanceL1() throws Exception {
    final double d1 = VecTools.distanceL1(new ArrayVec(1, 2, 3), new ArrayVec(1, 2, 3));
    assertEquals(0, d1, VectorsTest.EPSILON);

    final double d2 = VecTools.distanceL1(new ArrayVec(0, 0, 3), new ArrayVec(0, 2, 0));
    assertEquals(5, d2, VectorsTest.EPSILON);

    final double v3 = VecTools.distanceL1(new ArrayVec(0, 0), new ArrayVec(0, 0));
    assertEquals(0, v3, VectorsTest.EPSILON);
  }

  public void testDistanceL2() throws Exception {
    final double d1 = VecTools.distance(new ArrayVec(1, 2, 3), new ArrayVec(1, 2, 3));
    assertEquals(0, d1, VectorsTest.EPSILON);

    final double d2 = VecTools.distance(new ArrayVec(0, 0, 3), new ArrayVec(0, 4, 0));
    assertEquals(5, d2, VectorsTest.EPSILON);

    final double v3 = VecTools.distance(new ArrayVec(0, 0), new ArrayVec(0, 0));
    assertEquals(0, v3, VectorsTest.EPSILON);
  }

  public void testJoin() throws Exception {
    final Vec expected = new ArrayVec(1, 2, 3, 4);
    assertEquals(expected, VecTools.join(expected.sub(0, 2), expected.sub(2, 2)));
  }

  public void testSparseAppend() {
    FastRandom rng = new FastRandom(100500);
    Vec sparseA = new SparseVec(100);
    Vec sparseB = new SparseVec(100);
    Vec denseA = new ArrayVec(100);
    Vec denseB = new ArrayVec(100);
    for (int i = 0; i < 1000; i++) {
      VecTools.scale(denseA, 0);
      for (int j = 0; j < 100; j+= rng.nextPoisson(5)) {
        double val = rng.nextDouble();
        denseA.set(j, val);
      }
      VecTools.scale(denseB, 0);
      for (int j = 0; j < 100; j+= rng.nextPoisson(5)) {
        double val = rng.nextDouble();
        denseB.set(j, val);
      }
      VecTools.assign(sparseA, denseA);
      VecTools.assign(sparseB, denseB);
      assertEquals(VecTools.append(denseA, denseB), VecTools.append(sparseA, sparseB));
    }
  }

  public void testFillGaussianLong() {
    final Vec vec = new ArrayVec(100000);
    VecTools.fillGaussian(vec, new FastRandom(100500));
    final double mean = StatTools.mean(vec);
    final double variance = StatTools.variance(vec);
    assertTrue(Math.abs(mean) < 1e-2);
    assertTrue(Math.abs(variance - 1.0) < 1e-2);
  }

  public void testFillGaussian() {
    final Vec vec = new ArrayVec(100);
    VecTools.fillGaussian(vec, new FastRandom(100500));
    final double mean = StatTools.mean(vec);
    final double variance = StatTools.variance(vec);
    assertTrue(Math.abs(mean) < 1e-1);
    assertTrue(Math.abs(variance - 1.0) < 2e-1);
  }

}
