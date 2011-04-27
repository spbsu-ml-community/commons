package com.spbsu.commons.math.vectors;

import com.spbsu.commons.util.logging.Interval;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author vp
 */
public class FastVectorToolsTest extends TestCase {
  private static final int length = 10000;
  private static final int iterations = 10000;

  public void testScalarMultiply() throws Exception {
    final DVector<CharSequence> v1 = generateRandomVector(length);
    final DVector<CharSequence> v2 = generateRandomVector(length);

    assertEquals(VecTools.multiply(v1, v2), VecTools.multiply(v2, v1));

    Interval.start();
    for (int i = 0; i < iterations; i++) {
      VecTools.multiply(v1, v2);
    }
    Interval.stopAndPrint();
  }

  public void testMultiplyAll() throws Exception {
    List<DVector<CharSequence>> list = new ArrayList<DVector<CharSequence>>();
    for (int i = 0; i < 10000; i++) {
      list.add(generateRandomVector(length));
    }

    final DVector<CharSequence> v = generateRandomVector(length);

    double[] result = new double[0];
    for (int i = 0; i < 100; i++) {
      Interval.start();
      result = VecTools.multiplyAll(list, v);
      Interval.stopAndPrint();
    }
    Interval.start();
    for (int i = 0; i < result.length; i++) {
      assertEquals(VecTools.multiply(list.get(i), v), result[i]);
    }
    Interval.stopAndPrint();
  }

  public void testEuclideanNorm() throws Exception {
    final DVector<CharSequence> v1 = generateRandomVector(length);
    VecTools.scale(v1, 1 / VecTools.norm(v1));

    assertTrue(Math.abs(1. - VecTools.norm(v1)) < 0.0000000001);

    final int count = iterations * 10;

    Interval.start();
    for (int i = 0; i < count; i++) {
      VecTools.norm(v1);
    }
    Interval.stopAndPrint();
  }

  public void testSum() throws Exception {
    final DVector<CharSequence> v1 = generateRandomVector(length);
    final DVector<CharSequence> v2 = generateRandomVector(length);

    assertVectorsEqual(
        VecTools.append(VecTools.append(new DVector<CharSequence>(CharSequence.class), v1), v2),
        VecTools.append(VecTools.append(new DVector<CharSequence>(CharSequence.class), v2), v1));

    Interval.start();
    for (int i = 0; i < iterations; i++) {
      VecTools.append(new DVector<CharSequence>(CharSequence.class), v1, v2);
    }
    Interval.stopAndPrint();
  }

  public void testCosineEuclideanNorm() throws Exception {
    final DVector<CharSequence> v1 = generateRandomVector(length);
    final DVector<CharSequence> v2 = generateRandomVector(length);

    assertEquals(VecTools.cosine(v1, v2), VecTools.cosine(v1, v2));

    Interval.start();
    for (int i = 0; i < iterations; i++) {
      VecTools.cosine(v1, v2);
    }
    Interval.stopAndPrint();
  }

  public void testMultiply() throws Exception {
    final double factor = Math.PI;

    final DVector<CharSequence> v1 = generateRandomVector(length);

    assertVectorsEqual(VecTools.scale(v1, factor), VecTools.scale(v1, factor));

    Interval.start();
    for (int i = 0; i < iterations; i++) {
      VecTools.scale(v1, factor);
    }
    Interval.stopAndPrint();
  }

  public static <A> void assertVectorsEqual(final DVector<A> v1, final DVector<A> v2) {
    assertEquals(v1.nonZeroesCount(), v2.nonZeroesCount());
    final BasisVecIterator<A> iter1 = v1.iterator();
    final BasisVecIterator<A> iter2 = v2.iterator();
    while (iter1.advance()) {
      assertTrue(iter2.advance());
      assertEquals(iter1.key(), iter2.key());
      assertEquals(iter1.value(), iter2.value());
    }
  }

  public static DVector<CharSequence> generateRandomVector(final int length) {
    final Random random = new Random();
    final DVector<CharSequence> vector = new DVector<CharSequence>(CharSequence.class);
    for (int i = 0; i < length; i++) {
      final double d = random.nextDouble();
      if (d < 0.05) {
        vector.set("i" + i, d);
      }
    }
    return vector;
  }
}
