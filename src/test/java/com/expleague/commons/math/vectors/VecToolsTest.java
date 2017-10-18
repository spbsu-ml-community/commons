package com.expleague.commons.math.vectors;

import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
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
}
