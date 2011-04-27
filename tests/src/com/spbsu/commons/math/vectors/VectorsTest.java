package com.spbsu.commons.math.vectors;

import com.spbsu.commons.util.Factories;
import gnu.trove.TObjectDoubleProcedure;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

/**
 * User: terry
 * Date: 17.12.2009
 */
public class VectorsTest extends TestCase {

  public void testDoubleVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector = new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});
    assertEquals(0.0, vector.get("ha"));
    assertEquals(1.5, vector.get("h"));
    assertEquals(2.0, vector.get("hz"));

    final BasisVecIterator<CharSequence> iter = vector.iterator();
    assertTrue(iter.advance());
    assertEquals(iter.key(), "h");
    assertTrue(iter.advance());
    assertEquals(iter.key(), "hz");
    assertFalse(iter.advance());
  }

  public void testSumDoubleVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector = new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});
    final DVector<CharSequence> minusVector = new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{-1.5, -2});
    DVector<CharSequence> sum = VecTools.append(new DVector<CharSequence>(CharSequence.class), vector, vector);

    assertEquals(3.0, sum.get("h"));
    assertEquals(4.0, sum.get("hz"));

    assertEquals(0, VecTools.append(new DVector<CharSequence>(CharSequence.class), vector, minusVector).nonZeroesCount());
  }

  public void testDoubletoBinaryVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});
    VecTools.toBinary(vector);

    assertEquals(2, vector.nonZeroesCount());
    assertEquals(1.0, vector.get("h"));
    assertEquals(1.0, vector.get("hz"));
  }

  public void testScalarMultiplyDoubleVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});
    final DVector<CharSequence> vector2 =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});
    assertEquals(1.5 * 1.5 + 2 * 2, VecTools.multiply(vector, vector2));
  }

  public void testMultiplyDoubleVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});
    final DVector<CharSequence> newVector = VecTools.scale(vector, 2.0);

    assertEquals(2, newVector.nonZeroesCount());
    assertEquals(3.0, newVector.get("h"));
    assertEquals(4.0, newVector.get("hz"));
  }

  public void testCosineDoubleVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1, 1});
    final DVector<CharSequence> vector2 =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{2, 2});
    final DVector<CharSequence> vector3 =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1, 0});
    assertTrue(Math.abs(1.0 - VecTools.cosine(vector, vector2)) < 0.0001);
    assertTrue(Math.abs(Math.sqrt(2) / 2 - VecTools.cosine(vector, vector3)) < 0.00001);
  }

  public void testDistanceArrayVec() {
    final ArrayVec v1 = new ArrayVec(new double[2]);
    final ArrayVec v2 = new ArrayVec(1, 1);
    assertEquals(Math.sqrt(2), VecTools.distance(v1, v2));    
  }

  public void testTransformDoubleVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1, 1});
    final BasisVecIterator<CharSequence> iter = vector.iterator();
    while (iter.advance()) {
      iter.setValue(iter.value() / 2);
    }
    assertEquals(0.5, vector.get("h"));
    assertEquals(0.5, vector.get("hz"));
  }

  public void testEuclideanNormVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});

    assertEquals(Math.sqrt(1.5 * 1.5 + 2 * 2), VecTools.norm(vector));
  }

  public void testOmeNormVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});

    assertEquals(1.5 + 2.0, VecTools.norm1(vector));
  }

  public void testInfinityNormVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});

    assertEquals(2.0, VecTools.infinityNorm(vector));
  }

  public void testIterator() throws Exception {
    final DVector<CharSequence> v = new DVector<CharSequence>(
      new CharSequence[] {"1", "2", "3", "5", "10", "11", "13", "15"},
      new double[] {1, 1, 1, 1, 1, 1, 1, 1}
    );
    final HashSet<CharSequence> found = new HashSet<CharSequence>();
    v.forEach(new TObjectDoubleProcedure<CharSequence>() {
      @Override
      public boolean execute(CharSequence charSequence, double v) {
        found.add(charSequence);
        return true;
      }
    });
    assertEquals(8, found.size());
  }

}
