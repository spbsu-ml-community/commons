package com.spbsu.commons.math.vectors;

import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.random.GaussianRandomVec;
import com.spbsu.commons.util.ArrayTools;
import com.spbsu.commons.util.Factories;
import gnu.trove.TIntHashSet;
import gnu.trove.TObjectDoubleProcedure;
import junit.framework.TestCase;

import java.util.*;

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

  public void testDel2LastValues() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz", "ss", "asdasd");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2, 3, 0});
    final VecIterator iterator = vector.nonZeroes();
    while (iterator.advance()) {
      if(iterator.index() > 1)
        iterator.setValue(0);
    }
  }

  public void testIterator() throws Exception {
    final DVector<CharSequence> v = new DVector<CharSequence>(
      new CharSequence[] {"1", "2", "3", "5", "10", "11", "13", "15"},
      new double[] {1, 1, 1, 1, 1, 1, 1, 1}
    );
    final HashSet<CharSequence> found = new HashSet<CharSequence>();
    v.forEach(new TObjectDoubleProcedure<CharSequence>() {
      public boolean execute(CharSequence charSequence, double v) {
        found.add(charSequence);
        return true;
      }
    });
    assertEquals(8, found.size());
  }


  public void testInverseTriangle() {
    Mx a = new VecBasedMx(3, new ArrayVec(
            1, 0, 0,
            1, 2, 0,
            1, 2, 3
    ));
    final Mx inverse = VecTools.inverseLTriangle(a);
    assertEquals(VecTools.E(3), VecTools.multiply(a, inverse));
  }

  public void testMultiply() {
    Mx a = new VecBasedMx(3, new ArrayVec(
            1, 0, 0,
            1, 1, 0,
            1, 2, 3
    ));
    Mx b = new VecBasedMx(3, new ArrayVec(
            1, 0, 2,
            1, 2, 0,
            1, 2, 0
    ));
    Mx c = new VecBasedMx(3, new ArrayVec(
            1, 0, 2,
            2, 2, 2,
            6, 10, 2
    ));
    assertEquals(c, VecTools.multiply(a, b));
  }


  public void testInverse() {
    Mx a = new VecBasedMx(3, new ArrayVec(
            1, 1, 1,
            1, 2, 1,
            1, 1, 2
    ));
    final Mx l = VecTools.choleskyDecomposition(a);
    assertEquals(a, VecTools.multiply(l, VecTools.transpose(l)));
    final Mx inverseL = VecTools.inverseLTriangle(l);
    final Mx inverseA = VecTools.multiply(VecTools.transpose(inverseL), inverseL);
    assertEquals(VecTools.E(3), VecTools.multiply(a, inverseA));
  }

  public void testRandomInverse() {
    final int dim = 100;
    Random rand = new FastRandom(0);
    for (int c = 0; c < 100; c++) {
      final Mx a = new VecBasedMx(dim, dim);
      for (int i = 0; i < dim; i++) {
        for (int j = 0; j < i; j++) {
          final double val = rand.nextDouble();
          a.set(i, j, val);
          a.set(j, i, val);
        }
        a.set(i, i, Math.sqrt(dim));
      }
      final Mx l = VecTools.choleskyDecomposition(a);
      final Mx aa = VecTools.multiply(l, VecTools.transpose(l));
      if (VecTools.distance(a, aa) > 0.001)
        assertEquals(a, aa);
      final Mx inverseL = VecTools.inverseLTriangle(l);
      final Mx inverseA = VecTools.multiply(VecTools.transpose(inverseL), inverseL);
      assertTrue(VecTools.distance(VecTools.E(dim), VecTools.multiply(a, inverseA)) < 0.001);
    }
  }

  public void testMahalanobis3() {
    final Mx L = new VecBasedMx(3, new ArrayVec(new double[] {
            1, 0, 0,
            0.5, 1, 0,
            0.25, 0.3, 1
    }));
    final Mx Sigma = VecTools.multiply(L, VecTools.transpose(L));
    GaussianRandomVec randomVec = new GaussianRandomVec(new ArrayVec(3), Sigma, new Random());
    List<Vec> pool = new ArrayList<Vec>(100500);
    for (int i = 0; i < 10000; i++) {
      pool.add(randomVec.next());
    }

    Mx m = VecTools.mahalanobis(pool);
    assertTrue(VecTools.distance(L, VecTools.inverseLTriangle(m)) < 0.001);
  }

  public void testMahalanobis2() {
    final Mx L = new VecBasedMx(2, new ArrayVec(new double[] {
            1, 0,
            0.5, 1,
    }));
    final Mx Sigma = VecTools.multiply(L, VecTools.transpose(L));
    GaussianRandomVec randomVec = new GaussianRandomVec(new ArrayVec(2), Sigma, new Random());
    List<Vec> pool = new ArrayList<Vec>(100500);
    for (int i = 0; i < 10000; i++) {
      pool.add(randomVec.next());
    }

    Mx m = VecTools.mahalanobis(pool);
    assertTrue(VecTools.distance(L, VecTools.inverseLTriangle(m)) < 0.001);
  }

  public void testNearestNeighbour() {
    final Mx L = new VecBasedMx(2, new ArrayVec(new double[] {
            1, 0,
            0.5, 1,
    }));
    final Mx Sigma = VecTools.multiply(L, VecTools.transpose(L));
    final Random random = new FastRandom();
    GaussianRandomVec randomVec = new GaussianRandomVec(new ArrayVec(2), Sigma);
    List<Vec> pool = new ArrayList<Vec>();
    for (int i = 0; i < 10000; i++) {
      pool.add(randomVec.next());
    }
    LSHEuclidNNLocator locator = new LSHEuclidNNLocator(pool, 50, 200);
    double[] dist = new double[pool.size()];
    int[] order = new int[pool.size()];
    int mistakes = 0;
    for (int t = 0; t < 100; t++) {
      Vec current = pool.get(random.nextInt(pool.size()));
      for (int i = 0; i < pool.size(); i++) {
        dist[i] = VecTools.distance(current, pool.get(i));
        order[i] = i;
      }
      ArrayTools.parallelSort(dist, order);
      int[] nearestFound = new int[10];
      locator.nearest(current, 10, nearestFound, new double[10]);
      TIntHashSet found = new TIntHashSet(nearestFound);
      for (int i = 0; i < 10; i++) {
        mistakes += found.contains(order[i]) ? 0 : 1;
      }
    }
    assertTrue(mistakes < 10);
  }
}
