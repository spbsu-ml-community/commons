package com.spbsu.commons.math.vectors;

import java.util.*;


import com.spbsu.commons.math.vectors.impl.mx.VecBasedMx;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.math.vectors.impl.vectors.ConcatVec;
import com.spbsu.commons.math.vectors.impl.vectors.DVector;
import com.spbsu.commons.math.vectors.impl.vectors.SparseVec;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.random.GaussianRandomVec;
import com.spbsu.commons.util.ArrayTools;
import com.spbsu.commons.util.Factories;
import com.spbsu.commons.util.logging.Interval;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.set.hash.TIntHashSet;
import junit.framework.TestCase;

import static com.spbsu.commons.math.vectors.VecTools.*;

/**
 * User: terry
 * Date: 17.12.2009
 */
public class VectorsTest extends TestCase {

  public static final double EPSILON = 0.0001;

  public void testConcatVector() {
    final Vec origin = new ArrayVec(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    final ConcatVec cv = new ConcatVec(new ArrayVec(0, 1, 2, 3, 4), new ArrayVec(5, 6, 7, 8, 9));

    assertEquals(cv.dim(), origin.dim());
    VecTools.equals(cv, origin);

    final Vec cv2 = new ConcatVec(new ArrayVec(0, 1, 2, 3, 4), new SparseVec(10), new SparseVec(4, new int[]{0, 1, 2}, new double[]{1.0, 2.0, 3.0}), new ArrayVec(5, 6, 7, 8, 9));
    final Vec origin2 = new ArrayVec(0, 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 0, 5, 6, 7, 8, 9);

    assertEquals(cv2.dim(), origin2.dim());
    assertTrue(VecTools.equals(cv2, origin2));
    final VecIterator iter = cv2.nonZeroes();
    final VecIterator originIter = origin2.nonZeroes();
    while (originIter.advance()) {
      assertTrue(iter.advance());
      assertEquals(iter.value(), originIter.value());
    }
    assertFalse(iter.advance());

  }
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
    final DVector<CharSequence> sum = append(new DVector<CharSequence>(CharSequence.class), vector, vector);

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
    assertEquals(1.5 * 1.5 + 2 * 2, VecTools.multiply(vector, vector2));
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
      @Override
      public boolean execute(final CharSequence charSequence, final double v) {
        found.add(charSequence);
        return true;
      }
    });
    assertEquals(8, found.size());
  }


  public void testInverseTriangle() {
    final Mx a = new VecBasedMx(3, new ArrayVec(
            1, 0, 0,
            1, 2, 0,
            1, 2, 3
    ));
    final Mx inverse = MxTools.inverseLTriangle(a);
    assertEquals(MxTools.E(3), MxTools.multiply(a, inverse));
  }

  public void testOuterProduct() {
    final Vec u = new ArrayVec(1.0, 2.0);
    final Vec v = new ArrayVec(3.0, 4.0);

    final Mx expected = new VecBasedMx(2, new ArrayVec(3.0, 4.0,
                                                 6.0, 8.0));
    final Mx product = outer(u, v);
    for (int i = 0; i < product.dim(); i++) {
        assertEquals(expected.get(i), product.get(i), 1e-10);
    }
  }

  public void testMultiply() {
    final Mx a = new VecBasedMx(3, new ArrayVec(
            1, 0, 0,
            1, 1, 0,
            1, 2, 3
    ));
    final Mx b = new VecBasedMx(3, new ArrayVec(
            1, 0, 2,
            1, 2, 0,
            1, 2, 0
    ));
    final Mx c = new VecBasedMx(3, new ArrayVec(
            1, 0, 2,
            2, 2, 2,
            6, 10, 2
    ));
    assertEquals(c, MxTools.multiply(a, b));
  }

  public void testSparseMultiply() {
    final Mx a = new VecBasedMx(3, VecTools.copySparse(new ArrayVec(
            1, 0, 0,
            1, 1, 0,
            1, 2, 3
    )));
    final Mx b = new VecBasedMx(3, VecTools.copySparse(new ArrayVec(
            1, 0, 2,
            1, 2, 0,
            1, 2, 0
    )));
    final Mx c = new VecBasedMx(3, VecTools.copySparse(new ArrayVec(
            1, 0, 2,
            2, 2, 2,
            6, 10, 2
    )));
    assertEquals(c, MxTools.multiply(a, b));
  }


  public void testInverse() {
    final Mx a = new VecBasedMx(3, new ArrayVec(
            1, 1, 1,
            1, 2, 1,
            1, 1, 2
    ));
    final Mx l = MxTools.choleskyDecomposition(a);
    assertEquals(a, MxTools.multiply(l, MxTools.transpose(l)));
    final Mx inverseL = MxTools.inverseLTriangle(l);
    final Mx inverseA = MxTools.multiply(MxTools.transpose(inverseL), inverseL);
    assertEquals(MxTools.E(3), MxTools.multiply(a, inverseA));
  }

  public void testRandomInverse() {
    final int dim = 100;
    final Random rand = new FastRandom(0);
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
      final Mx l = MxTools.choleskyDecomposition(a);
      final Mx aa = MxTools.multiply(l, MxTools.transpose(l));
      if (distance(a, aa) > 0.001)
        assertEquals(a, aa);
      final Mx inverseL = MxTools.inverseLTriangle(l);
      final Mx inverseA = MxTools.multiply(MxTools.transpose(inverseL), inverseL);
      assertTrue(distance(MxTools.E(dim), MxTools.multiply(a, inverseA)) < 0.001);
    }
  }

  public void testMahalanobis3() {
    final Mx L = new VecBasedMx(3, new ArrayVec(new double[] {
            1, 0, 0,
            0.5, 1, 0,
            0.25, 0.3, 1
    }));
    final Mx Sigma = MxTools.multiply(L, MxTools.transpose(L));
    final GaussianRandomVec randomVec = new GaussianRandomVec(new ArrayVec(3), Sigma, new Random());
    final List<Vec> pool = new ArrayList<Vec>(100500);
    for (int i = 0; i < 20000; i++) {
      pool.add(randomVec.next());
    }

    final Mx m = MxTools.mahalanobis(pool);
    assertTrue(distance(L, MxTools.inverseLTriangle(m)) < 0.1);
  }

  //TODO[solar]: fix test with random seed equals to '1408619692919L'
  public void _testMahalanobis2() {
    final Mx L = new VecBasedMx(2, new ArrayVec(new double[] {
            1, 0,
            0.5, 1,
    }));
    final Mx Sigma = MxTools.multiply(L, MxTools.transpose(L));
    final GaussianRandomVec randomVec = new GaussianRandomVec(new ArrayVec(2), Sigma, new Random(1408619692919L));
    final List<Vec> pool = new ArrayList<Vec>(100500);
    for (int i = 0; i < 20000; i++) {
      pool.add(randomVec.next());
    }

    final Mx m = MxTools.mahalanobis(pool);
    assertTrue("Distance is: " + Double.toString(distance(L, MxTools.inverseLTriangle(m))), distance(L, MxTools.inverseLTriangle(m)) < 0.02);
  }

  //TODO[solar]: fix test with random seed equals to '1408621387063L'
  public void _testNearestNeighbour() {
    final Mx L = new VecBasedMx(2, new ArrayVec(new double[] {
            1, 0,
            0.5, 1,
    }));
    final Mx Sigma = MxTools.multiply(L, MxTools.transpose(L));
    final Random random = new FastRandom(1408621387063L);
    final GaussianRandomVec randomVec = new GaussianRandomVec(new ArrayVec(2), Sigma, random);
    final List<Vec> pool = new ArrayList<Vec>();
    for (int i = 0; i < 10000; i++) {
      pool.add(randomVec.next());
    }
    final LSHEuclidNNLocator locator = new LSHEuclidNNLocator(pool, 50, 200, random);
    final double[] dist = new double[pool.size()];
    final int[] order = new int[pool.size()];
    int mistakes = 0;
    for (int t = 0; t < 100; t++) {
      final Vec current = pool.get(random.nextInt(pool.size()));
      for (int i = 0; i < pool.size(); i++) {
        dist[i] = distance(current, pool.get(i));
        order[i] = i;
      }
      ArrayTools.parallelSort(dist, order);
      final int[] nearestFound = new int[10];
      locator.nearest(current, 10, nearestFound, new double[10]);
      final TIntHashSet found = new TIntHashSet(nearestFound);
      for (int i = 0; i < 10; i++) {
        mistakes += found.contains(order[i]) ? 0 : 1;
      }
    }
    assertTrue(mistakes < 20);
  }

  public void testHHLQ() {
    final ArrayVec vec = new ArrayVec(
            1, 1, 1,
            1, 2, 2,
            1, 2, 3
    );
    final Mx a = new VecBasedMx(3, vec);
    final Mx l = new VecBasedMx(3, 3);
    final Mx q = new VecBasedMx(3, 3);

    MxTools.householderLQ(a, l, q);
    assertTrue(distance(MxTools.multiply(MxTools.transpose(q), q), MxTools.E(3)) < 0.00001);
    assertTrue(distance(a, MxTools.multiply(l, MxTools.transpose(q))) < 0.0001);
  }

  public void testEigenDecomposition() {
    final ArrayVec vec = new ArrayVec(
            1, 1, 1,
            1, 2, 2,
            1, 2, 3
    );
    final Mx a = new VecBasedMx(3, vec);
    final Mx q = new VecBasedMx(3, 3);
    final Mx sigma = new VecBasedMx(3, 3);
    MxTools.eigenDecomposition(a, q, sigma);
    final Mx result = MxTools.multiply(MxTools.transpose(q), MxTools.multiply(sigma, q));
    assertTrue(distance(a, result) < 0.001);
  }

  public void testHHLQInverse() {
    final ArrayVec vec = new ArrayVec(
            1, 1, 1,
            1, 2, 2,
            1, 2, 3
    );
    final Mx a = new VecBasedMx(3, vec);
    final Mx l = new VecBasedMx(3, 3);
    final Mx qt = new VecBasedMx(3, 3);

    MxTools.householderLQ(a, l, qt);
    final Mx invL = MxTools.inverseLTriangle(l);
    final Mx invA = MxTools.multiply(qt, invL);
    assertTrue(distance(MxTools.E(3), MxTools.multiply(invA, a)) < 0.0001);
  }

  public void testHHLQInverseBad1() {
    final ArrayVec vec = new ArrayVec(
            8956, 3347.849987, 2846.879988, 2761.270001,
            3347.849987, 1493.862294, 1028.206595, 825.7811009,
            2846.879988, 1028.206595, 1156.55439, 662.1189985,
            2761.270001, 825.7811009, 662.1189985, 1273.369898
    );
    final Mx a = new VecBasedMx(4, vec);
    final Mx l = new VecBasedMx(4, 4);
    final Mx qt = new VecBasedMx(4, 4);

    MxTools.householderLQ(a, l, qt);
    final Mx invL = MxTools.inverseLTriangle(l);
    final Mx invA = MxTools.multiply(qt, invL);
    assertTrue(distance(MxTools.E(4), MxTools.multiply(invA, a)) < 0.0001);
  }

  public void testHHLQInverseBad2() {
    final ArrayVec vec = new ArrayVec(
            10131, 3599.009986, 3669.579995, 20.63053123, 2862.410003,
            3599.009986, 1531.622894, 1234.652796, 7.197349856, 832.7343011,
            3669.579995, 1234.652796, 1675.474395, 6.646254717, 759.4528004,
            20.63053123, 7.197349856, 6.646254717, 0.5007998021, 6.786926655,
            2862.410003, 832.7343011, 759.4528004, 6.786926655, 1270.2229
    );
    final Mx a = new VecBasedMx(5, vec);
    final Mx l = new VecBasedMx(5, 5);
    final Mx qt = new VecBasedMx(5, 5);

    MxTools.householderLQ(a, l, qt);
    final Mx invL = MxTools.inverseLTriangle(l);
    final Mx invA = MxTools.multiply(qt, invL);
    assertTrue(distance(MxTools.E(5), MxTools.multiply(invA, a)) < EPSILON);
  }

  public void testSubMx1() {
    final Mx a = new VecBasedMx(3, new ArrayVec(
            1, 0, 0,
            1, 2, 0,
            1, 2, 3
    ));
    matrixTest(a);
  }

  public void testSubMxSparse1() {
    final Mx a = new VecBasedMx(3, copySparse(new ArrayVec(
            1, 0, 0,
            1, 2, 0,
            1, 2, 3
    )));
    matrixTest(a);
  }

  public void testSubMxInverse1() {
    final Mx temp = new VecBasedMx(4, copySparse(new ArrayVec(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 1, 2, 0,
            0, 1, 2, 3
    )));
    final Mx sub = temp.sub(1, 1, 3, 3);
    final Mx a = new VecBasedMx(3, copySparse(new ArrayVec(
            1, 0, 0,
            1, 2, 0,
            1, 2, 3
    )));
    final Mx invSub = MxTools.inverseLTriangle(sub);
    final Mx invA = MxTools.inverseLTriangle(a);
    assertTrue(distance(invSub, invA) < EPSILON);
  }

  public void testSubMxWriteSparse() {
    final Mx refResult = new VecBasedMx(4, copySparse(new ArrayVec(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 1, 2, 0,
            0, 1, 2, 3
    )));
    final Mx temp = MxTools.E(4);
    final Mx sub = temp.sub(1, 1, 3, 3);
    final Mx a = new VecBasedMx(3, copySparse(new ArrayVec(
            1, 0, 0,
            1, 2, 0,
            1, 2, 3
    )));
    assign(sub, a);
    assertTrue(distance(refResult, temp) < EPSILON);
  }

  public void testSubMxWrite() {
    final Mx refResult = new VecBasedMx(4, new ArrayVec(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 1, 2, 0,
            0, 1, 2, 3
    ));
    final Mx temp = MxTools.E(4);
    final Mx sub = temp.sub(1, 1, 3, 3);
    final Mx a = new VecBasedMx(3, copySparse(new ArrayVec(
            1, 0, 0,
            1, 2, 0,
            1, 2, 3
    )));
    assign(sub, a);
    assertTrue(distance(refResult, temp) < EPSILON);
  }

  public void testSubSubMx() {
    final Mx full = new VecBasedMx(4, new ArrayVec(new double[]{
            0, 1, 2, 3,
            1, 2, 3, 4,
            2, 3, 4, 5,
            3, 4, 5, 6
    }));

    final Mx sub3x3 = full.sub(0, 0, 3, 3);

    final Vec rowSub = sub3x3.row(1);
    assertTrue(Arrays.equals(rowSub.toArray(), new double[]{1, 2, 3}));

    final Vec colSub = sub3x3.col(2);
    assertTrue(Arrays.equals(colSub.toArray(), new double[]{2, 3, 4}));

    final Mx subSub = sub3x3.sub(1, 1, 3, 2);
    assertTrue(Arrays.equals(subSub.toArray(), new double[]{2, 3,
        3, 4,
        4, 5})
    );
  }

  public void testStressMultiply() {
    final Random rnd = new FastRandom();
    final Mx temp = MxTools.E(1000);
    Interval.start();
    Interval.suspend();
    for (int i = 0; i < 10; i++) {
      final Mx mx = new VecBasedMx(1000, new ArrayVec(1000 * 1000));
      for (int j = 0; j < mx.dim(); j++)
        mx.set(j, rnd.nextGaussian());
      Interval.resume();
      final Mx result = MxTools.multiply(temp, mx);
      Interval.suspend();
      if (distance(mx, result) > EPSILON)
        assertTrue(distance(mx, result) < EPSILON);
    }
    Interval.stopAndPrint();
  }

  public void testStressMultiplySparse() {
    final Random rnd = new FastRandom();
    final Mx temp = MxTools.sparseE(1000);
    Interval.start();
    Interval.suspend();
    for (int i = 0; i < 10; i++) {
      final Mx mx = new VecBasedMx(1000, new ArrayVec(1000 * 1000));
      for (int j = 0; j < mx.dim(); j++)
        mx.set(j, rnd.nextGaussian());
      Interval.resume();
      final Mx result = MxTools.multiply(temp, mx);
      Interval.suspend();
      if (distance(mx, result) > EPSILON)
        assertTrue(distance(mx, result) < EPSILON);
    }
    Interval.stopAndPrint();
  }

  public void notestStressMultiplyDSparse() {
    final Random rnd = new FastRandom();
    Interval.start();
    Interval.suspend();
    for (int i = 0; i < 1; i++) {
      final Mx mxA = new VecBasedMx(1000, new ArrayVec(1000 * 1000));
      for (int j = 0; j < mxA.dim(); j++)
        if (rnd.nextDouble() < 0.05)
          mxA.set(j, rnd.nextGaussian());
      final Mx mxB = new VecBasedMx(1000, new ArrayVec(1000 * 1000));
      for (int j = 0; j < mxB.dim(); j++)
        if (rnd.nextDouble() < 0.05)
          mxB.set(j, rnd.nextGaussian());
      Interval.resume();
      Mx resultDense = null;
      for (int t = 0; t < 1; t++)
        resultDense = MxTools.multiply(mxA, mxB);
      Interval.suspend();

      final Mx sparseA = new VecBasedMx(1000, copySparse(((VecBasedMx)mxA).vec));
      final Mx sparseB = new VecBasedMx(1000, copySparse(((VecBasedMx)mxB).vec));
      Mx resultSparse = null;
      Interval.resume();
      for (int t = 0; t < 10; t++)
        resultSparse = MxTools.multiply(sparseA, sparseB);
      Interval.suspend();
      if (distance(resultDense, resultSparse) > EPSILON)
        assertTrue(distance(resultDense, resultSparse) < EPSILON);
    }
    Interval.stopAndPrint();
  }

  private void matrixTest(final Mx a) {
    for (int k = 0; k < 3; k++) {
      final Mx col = a.sub(0, k, 3, 1);
      final MxIterator iterator = col.nonZeroes();
      int i;
      for (i = 0; iterator.advance() && i < a.rows(); i++) {
        assertEquals(a.get(iterator.index(), k), iterator.value());
      }
      assertTrue(!iterator.isValid() && (i == a.rows() - k));
    }
  }

  public void testCheckOrthogonality() {
    final Mx mx = new VecBasedMx(2, new ArrayVec(
        0.96, -0.28,
        0.28, 0.96
    ));
    assertTrue(VecTools.checkOrthogonality(mx));
  }

  public void testSubtract() {
    final Vec a = new ArrayVec(1, 2, 3);
    final Vec b = new ArrayVec(2, 2, 3);
    final Vec subtract = VecTools.subtract(a, b);
    assertEquals(new ArrayVec(-1, 0, 0), subtract);
  }

  public void testSubtract2() {
    final Mx a = new VecBasedMx(2, new ArrayVec(0, 1, 0, 1));
    final Mx b = new VecBasedMx(2, new ArrayVec(0, 0, 0, 1));
    final Vec subtract = VecTools.subtract(a, b);
    assertEquals(new VecBasedMx(2, new ArrayVec(0, 1, 0, 0)), subtract);
  }

  public void testSubtract3() throws Exception {
    final Mx a = new VecBasedMx(2, new ArrayVec(1, 0, 0, 1));
    final Mx b = new VecBasedMx(2, new ArrayVec(1, 1, 0, 1));
    final Mx subA = a.sub(0, 0, 1, 2);
    final Mx subB = b.sub(0, 0, 1, 2);
    final Vec subtract = VecTools.subtract(subA, subB);
    assertEquals(new VecBasedMx(2, new ArrayVec(0, -1)), subtract);
  }

  public void testSparseVec() throws Exception {
    final SparseVec sparseVec = new SparseVec(4, new int[]{0, 1, 2}, new double[]{1.0, 2.0, 3.0});  //{1, 2, 3, 0}
    sparseVec.adjust(3, 1.0);
    assertTrue(Arrays.equals(new double[]{1, 2, 3, 1}, sparseVec.toArray()));

    sparseVec.adjust(1, -2.0);
    assertTrue(Arrays.equals(new double[]{1, 0, 3, 1}, sparseVec.toArray()));

    sparseVec.set(2, 0);
    assertTrue(Arrays.equals(new double[]{1, 0, 0, 1}, sparseVec.toArray()));
  }

  public void testSparseVecNZIterator() throws Exception {
    final SparseVec sparseVec = new SparseVec(4, new int[]{0, 1, 2}, new double[]{1.0, 2.0, 3.0});  //{1, 2, 3, 0}
    final VecIterator vecIterator = sparseVec.nonZeroes();

    vecIterator.advance();
    assertEquals(1.0, vecIterator.value());

    vecIterator.advance();
    vecIterator.setValue(0.0);
    assertEquals(0.0, vecIterator.value());

    vecIterator.advance();
    vecIterator.setValue(999.0);
    assertEquals(999.0, vecIterator.value());

    assertFalse(vecIterator.advance());
    assertTrue(Arrays.equals(new double[] {1.0, 0.0, 999.0, 0.0}, sparseVec.toArray()));
  }

  public void testToString() {
    final String str = new ArrayVec(0, 1, 2, 3).toString();
    assertEquals("4 0 1 2 3", str);
  }

  public void testArgmax() throws Exception {
    final Vec vec = new ArrayVec(1, 4, 2, 5, 9);
    assertEquals(4, VecTools.argmax(vec));
  }

  public void testCutSparseVec() throws Exception {
    final SparseVec sparseVec = new SparseVec(5, new int[]{1, 2, 4}, new double[]{0.1, 0.2, 0.4});
    final int[] indexesToCut = {0, 1, 4};

    final int[] expectedIdxs = {1, 2};
    final double[] expectedValues = {0.1, 0.4};
    final SparseVec cutVec = VecTools.cutSparseVec(sparseVec, indexesToCut);

    assertTrue(Arrays.equals(expectedIdxs, cutVec.indices.toArray()));
    assertTrue(Arrays.equals(expectedValues, cutVec.values.toArray()));
  }
}
