package com.spbsu.commons.math.vectors;

import com.spbsu.commons.math.vectors.impl.ArrayVec;
import com.spbsu.commons.math.vectors.impl.SparseVec;
import com.spbsu.commons.math.vectors.impl.VecBasedMx;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.random.GaussianRandomVec;
import com.spbsu.commons.util.ArrayTools;
import com.spbsu.commons.util.Factories;

import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.set.hash.TIntHashSet;
import junit.framework.TestCase;
import util.Interval;

import java.util.*;

import static com.spbsu.commons.math.vectors.VecTools.*;
import static com.spbsu.commons.math.vectors.VecTools.multiply;

/**
 * User: terry
 * Date: 17.12.2009
 */
public class VectorsTest extends TestCase {

  public static final double EPSILON = 0.0001;

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
    DVector<CharSequence> sum = append(new DVector<CharSequence>(CharSequence.class), vector, vector);

    assertEquals(3.0, sum.get("h"));
    assertEquals(4.0, sum.get("hz"));

    assertEquals(false, append(new DVector<CharSequence>(CharSequence.class), vector, minusVector).nonZeroes().advance());
  }

  public void testDoubletoBinaryVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});
    toBinary(vector);

    assertEquals(1.0, vector.get("h"));
    assertEquals(1.0, vector.get("hz"));
  }

  public void testScalarMultiplyDoubleVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});
    final DVector<CharSequence> vector2 =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});
    assertEquals(1.5 * 1.5 + 2 * 2, multiply(vector, vector2));
  }

  public void testMultiplyDoubleVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});
    final DVector<CharSequence> newVector = scale(vector, 2.0);

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
    assertTrue(Math.abs(1.0 - cosine(vector, vector2)) < 0.0001);
    assertTrue(Math.abs(Math.sqrt(2) / 2 - cosine(vector, vector3)) < 0.00001);
  }

  public void testDistanceArrayVec() {
    final ArrayVec v1 = new ArrayVec(new double[2]);
    final ArrayVec v2 = new ArrayVec(1, 1);
    assertEquals(Math.sqrt(2), distance(v1, v2));
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

    assertEquals(Math.sqrt(1.5 * 1.5 + 2 * 2), norm(vector));
  }

  public void testOmeNormVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});

    assertEquals(1.5 + 2.0, norm1(vector));
  }

  public void testInfinityNormVector() {
    final Set<CharSequence> axes = Factories.<CharSequence>linkedHashSet("h", "hz");
    final DVector<CharSequence> vector =
        new DVector<CharSequence>(axes.toArray(new CharSequence[0]), new double[]{1.5, 2});

    assertEquals(2.0, infinityNorm(vector));
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
    final Mx inverse = inverseLTriangle(a);
    assertEquals(E(3), multiply(a, inverse));
  }

  public void testOuterProduct() {
    Vec u = new ArrayVec(1.0, 2.0);
    Vec v = new ArrayVec(3.0, 4.0);

    Mx expected = new VecBasedMx(2, new ArrayVec(3.0, 4.0,
                                                 6.0, 8.0));
    Mx product = outer(u, v);
    for (int i = 0; i < product.dim(); i++) {
        assertEquals(expected.get(i), product.get(i), 1e-10);
    }
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
    assertEquals(c, multiply(a, b));
  }

  public void testSparseMultiply() {
    Mx a = new VecBasedMx(3, VecTools.copySparse(new ArrayVec(
            1, 0, 0,
            1, 1, 0,
            1, 2, 3
    )));
    Mx b = new VecBasedMx(3, VecTools.copySparse(new ArrayVec(
            1, 0, 2,
            1, 2, 0,
            1, 2, 0
    )));
    Mx c = new VecBasedMx(3, VecTools.copySparse(new ArrayVec(
            1, 0, 2,
            2, 2, 2,
            6, 10, 2
    )));
    assertEquals(c, multiply(a, b));
  }


  public void testInverse() {
    Mx a = new VecBasedMx(3, new ArrayVec(
            1, 1, 1,
            1, 2, 1,
            1, 1, 2
    ));
    final Mx l = choleskyDecomposition(a);
    assertEquals(a, multiply(l, transpose(l)));
    final Mx inverseL = inverseLTriangle(l);
    final Mx inverseA = multiply(transpose(inverseL), inverseL);
    assertEquals(E(3), multiply(a, inverseA));
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
      final Mx l = choleskyDecomposition(a);
      final Mx aa = multiply(l, transpose(l));
      if (distance(a, aa) > 0.001)
        assertEquals(a, aa);
      final Mx inverseL = inverseLTriangle(l);
      final Mx inverseA = multiply(transpose(inverseL), inverseL);
      assertTrue(distance(E(dim), multiply(a, inverseA)) < 0.001);
    }
  }

  public void testMahalanobis3() {
    final Mx L = new VecBasedMx(3, new ArrayVec(new double[] {
            1, 0, 0,
            0.5, 1, 0,
            0.25, 0.3, 1
    }));
    final Mx Sigma = multiply(L, transpose(L));
    GaussianRandomVec randomVec = new GaussianRandomVec(new ArrayVec(3), Sigma, new Random());
    List<Vec> pool = new ArrayList<Vec>(100500);
    for (int i = 0; i < 20000; i++) {
      pool.add(randomVec.next());
    }

    Mx m = mahalanobis(pool);
    assertTrue(distance(L, inverseLTriangle(m)) < 0.02);
  }

  public void testMahalanobis2() {
    final Mx L = new VecBasedMx(2, new ArrayVec(new double[] {
            1, 0,
            0.5, 1,
    }));
    final Mx Sigma = multiply(L, transpose(L));
    GaussianRandomVec randomVec = new GaussianRandomVec(new ArrayVec(2), Sigma, new Random());
    List<Vec> pool = new ArrayList<Vec>(100500);
    for (int i = 0; i < 20000; i++) {
      pool.add(randomVec.next());
    }

    Mx m = mahalanobis(pool);
    assertTrue(distance(L, inverseLTriangle(m)) < 0.02);
  }

  public void testNearestNeighbour() {
    final Mx L = new VecBasedMx(2, new ArrayVec(new double[] {
            1, 0,
            0.5, 1,
    }));
    final Mx Sigma = multiply(L, transpose(L));
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
        dist[i] = distance(current, pool.get(i));
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

  public void testHHLQ() {
    final ArrayVec vec = new ArrayVec(
            1, 1, 1,
            1, 2, 2,
            1, 2, 3
    );
    Mx a = new VecBasedMx(3, vec);
    Mx l = new VecBasedMx(3, 3);
    Mx q = new VecBasedMx(3, 3);

    householderLQ(a, l, q);
    assertTrue(distance(multiply(transpose(q), q), E(3)) < 0.00001);
    assertTrue(distance(a, multiply(l, transpose(q))) < 0.0001);
  }

  public void testEigenDecomposition() {
    final ArrayVec vec = new ArrayVec(
            1, 1, 1,
            1, 2, 2,
            1, 2, 3
    );
    Mx a = new VecBasedMx(3, vec);
    Mx q = new VecBasedMx(3, 3);
    Mx sigma = new VecBasedMx(3, 3);
    eigenDecomposition(a, q, sigma);
    Mx result = multiply(transpose(q), multiply(sigma, q));
    assertTrue(distance(a, result) < 0.001);
  }

  public void testHHLQInverse() {
    final ArrayVec vec = new ArrayVec(
            1, 1, 1,
            1, 2, 2,
            1, 2, 3
    );
    Mx a = new VecBasedMx(3, vec);
    Mx l = new VecBasedMx(3, 3);
    Mx qt = new VecBasedMx(3, 3);

    householderLQ(a, l, qt);
    final Mx invL = inverseLTriangle(l);
    final Mx invA = multiply(qt, invL);
    assertTrue(distance(E(3), multiply(invA, a)) < 0.0001);
  }

  public void testHHLQInverseBad1() {
    final ArrayVec vec = new ArrayVec(
            8956, 3347.849987, 2846.879988, 2761.270001,
            3347.849987, 1493.862294, 1028.206595, 825.7811009,
            2846.879988, 1028.206595, 1156.55439, 662.1189985,
            2761.270001, 825.7811009, 662.1189985, 1273.369898
    );
    Mx a = new VecBasedMx(4, vec);
    Mx l = new VecBasedMx(4, 4);
    Mx qt = new VecBasedMx(4, 4);

    householderLQ(a, l, qt);
    final Mx invL = inverseLTriangle(l);
    final Mx invA = multiply(qt, invL);
    assertTrue(distance(E(4), multiply(invA, a)) < 0.0001);
  }

  public void testHHLQInverseBad2() {
    final ArrayVec vec = new ArrayVec(
            10131, 3599.009986, 3669.579995, 20.63053123, 2862.410003,
            3599.009986, 1531.622894, 1234.652796, 7.197349856, 832.7343011,
            3669.579995, 1234.652796, 1675.474395, 6.646254717, 759.4528004,
            20.63053123, 7.197349856, 6.646254717, 0.5007998021, 6.786926655,
            2862.410003, 832.7343011, 759.4528004, 6.786926655, 1270.2229
    );
    Mx a = new VecBasedMx(5, vec);
    Mx l = new VecBasedMx(5, 5);
    Mx qt = new VecBasedMx(5, 5);

    householderLQ(a, l, qt);
    final Mx invL = inverseLTriangle(l);
    final Mx invA = multiply(qt, invL);
    assertTrue(distance(E(5), multiply(invA, a)) < EPSILON);
  }

  public void testSubMx1() {
    Mx a = new VecBasedMx(3, new ArrayVec(
            1, 0, 0,
            1, 2, 0,
            1, 2, 3
    ));
    matrixTest(a);
  }

  public void testSubMxSparse1() {
    Mx a = new VecBasedMx(3, copySparse(new ArrayVec(
            1, 0, 0,
            1, 2, 0,
            1, 2, 3
    )));
    matrixTest(a);
  }

  public void testSubMxInverse1() {
    Mx temp = new VecBasedMx(4, copySparse(new ArrayVec(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 1, 2, 0,
            0, 1, 2, 3
    )));
    Mx sub = temp.sub(1, 1, 3, 3);
    Mx a = new VecBasedMx(3, copySparse(new ArrayVec(
            1, 0, 0,
            1, 2, 0,
            1, 2, 3
    )));
    final Mx invSub = inverseLTriangle(sub);
    final Mx invA = inverseLTriangle(a);
    assertTrue(distance(invSub, invA) < EPSILON);
  }

  public void testSubMxWriteSparse() {
    Mx refResult = new VecBasedMx(4, copySparse(new ArrayVec(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 1, 2, 0,
            0, 1, 2, 3
    )));
    Mx temp = E(4);
    Mx sub = temp.sub(1, 1, 3, 3);
    Mx a = new VecBasedMx(3, copySparse(new ArrayVec(
            1, 0, 0,
            1, 2, 0,
            1, 2, 3
    )));
    assign(sub, a);
    assertTrue(distance(refResult, temp) < EPSILON);
  }

  public void testSubMxWrite() {
    Mx refResult = new VecBasedMx(4, new ArrayVec(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 1, 2, 0,
            0, 1, 2, 3
    ));
    Mx temp = E(4);
    Mx sub = temp.sub(1, 1, 3, 3);
    Mx a = new VecBasedMx(3, copySparse(new ArrayVec(
            1, 0, 0,
            1, 2, 0,
            1, 2, 3
    )));
    assign(sub, a);
    assertTrue(distance(refResult, temp) < EPSILON);
  }

  public void testStressMultiply() {
    Random rnd = new FastRandom();
    Mx temp = E(1000);
    Interval.start();
    Interval.suspend();
    for (int i = 0; i < 10; i++) {
      Mx mx = new VecBasedMx(1000, new ArrayVec(1000 * 1000));
      for (int j = 0; j < mx.dim(); j++)
        mx.set(j, rnd.nextGaussian());
      Interval.resume();
      Mx result = multiply(temp, mx);
      Interval.suspend();
      if (distance(mx, result) > EPSILON)
        assertTrue(distance(mx, result) < EPSILON);
    }
    Interval.stopAndPrint();
  }

  public void testStressMultiplySparse() {
    Random rnd = new FastRandom();
    Mx temp = sparseE(1000);
    Interval.start();
    Interval.suspend();
    for (int i = 0; i < 10; i++) {
      Mx mx = new VecBasedMx(1000, new ArrayVec(1000 * 1000));
      for (int j = 0; j < mx.dim(); j++)
        mx.set(j, rnd.nextGaussian());
      Interval.resume();
      Mx result = multiply(temp, mx);
      Interval.suspend();
      if (distance(mx, result) > EPSILON)
        assertTrue(distance(mx, result) < EPSILON);
    }
    Interval.stopAndPrint();
  }

  public void testStressMultiplyDSparse() {
    Random rnd = new FastRandom();
    Interval.start();
    Interval.suspend();
    for (int i = 0; i < 1; i++) {
      Mx mxA = new VecBasedMx(1000, new ArrayVec(1000 * 1000));
      for (int j = 0; j < mxA.dim(); j++)
        if (rnd.nextDouble() < 0.05)
          mxA.set(j, rnd.nextGaussian());
      Mx mxB = new VecBasedMx(1000, new ArrayVec(1000 * 1000));
      for (int j = 0; j < mxB.dim(); j++)
        if (rnd.nextDouble() < 0.05)
          mxB.set(j, rnd.nextGaussian());
      Interval.resume();
      Mx resultDense = null;
      for (int t = 0; t < 1; t++)
        resultDense = multiply(mxA, mxB);
      Interval.suspend();

      Mx sparseA = new VecBasedMx(1000, copySparse(((VecBasedMx)mxA).vec));
      Mx sparseB = new VecBasedMx(1000, copySparse(((VecBasedMx)mxB).vec));
      Mx resultSparse = null;
      Interval.resume();
      for (int t = 0; t < 10; t++)
        resultSparse = multiply(sparseA, sparseB);
      Interval.suspend();
      if (distance(resultDense, resultSparse) > EPSILON)
        assertTrue(distance(resultDense, resultSparse) < EPSILON);
    }
    Interval.stopAndPrint();
  }

  private void matrixTest(Mx a) {
    for (int k = 0; k < 3; k++) {
      Mx col = a.sub(0, k, 3, 1);
      MxIterator iterator = col.nonZeroes();
      int i;
      for (i = 0; iterator.advance() && i < a.rows(); i++) {
        assertEquals(a.get(iterator.index(), k), iterator.value());
      }
      assertTrue(!iterator.isValid() && (i == a.rows() - k));
    }
  }

}
