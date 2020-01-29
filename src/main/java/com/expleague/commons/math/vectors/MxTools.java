package com.expleague.commons.math.vectors;

import com.expleague.commons.math.AnalyticFunc;
import com.expleague.commons.math.MathTools;
import com.expleague.commons.math.vectors.impl.mx.SparseMx;
import com.expleague.commons.math.vectors.impl.mx.VecBasedMx;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.math.vectors.impl.vectors.SparseVec;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.util.ArrayTools;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static com.expleague.commons.math.vectors.VecTools.*;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 * User: qdeee
 * Date: 05.06.14
 */
public class MxTools {
  private static final Logger log = Logger.getLogger(MxTools.class.getName());
  private static final double EPSILON = 1e-5;

  public static boolean checkSymmetry(final Mx a) {
    if (a.columns() != a.rows())
      return false;
    final int dim = a.columns();
    for (int i = 0; i < dim; i++)
      for (int j = 0; j < dim; j++)
        if (abs(a.get(i, j) - a.get(j, i)) > EPSILON)
          return false;
    return true;
  }

  public static Vec rowSum(final Mx a) {
    final Vec result = new ArrayVec(a.rows());
    for (int row = 0; row < a.rows(); ++row) {
      double val = 0;
      for (int col = 0; col < a.columns(); ++col) {
        val += a.get(row, col);
      }
      result.set(row, val);
    }
    return result;
  }

  public static Mx inverse(final Mx A) {
    Mx L = new VecBasedMx(A.rows(), A.columns());
    final Mx Q = new VecBasedMx(A.rows(), A.columns());
    householderLQ(A, L, Q);
    L = inverseLTriangle(L);
    final Mx Inv = MxTools.multiply(Q, L);
    return Inv;
  }

  public static Mx laplacian(final Mx a) {
    final Vec d = rowSum(a);
    final Mx L = new VecBasedMx(a.rows(), a.columns());
    for (int i = 0; i < a.rows(); ++i) {
      for (int j = i + 1; j < a.columns(); ++j) {
        final double val = -a.get(i, j);
        L.set(i, j, val);
        L.set(j, i, val);
      }
    }
    for (int i = 0; i < a.rows(); ++i) {
      final double val = d.get(i) - a.get(i, i);
      L.set(i, i, val);
    }
    return L;
  }

  public static Mx choleskyDecomposition(final Mx a) {
    if (a.columns() != a.rows())
      throw new IllegalArgumentException("Matrix must be square for Cholesky decomposition!");
    if (!checkSymmetry(a))
      throw new IllegalArgumentException("Matrix must be symmetric!");
    final Mx l = new VecBasedMx(a.columns(), a.columns());
    // Cholesky–Banachiewicz schema
    for (int i = 0; i < a.rows(); i++) {
      double sum2 = 0;
      for (int j = 0; j < i; j++) {
        double val = a.get(i, j);
        for (int k = 0; k < j; k++) {
          val -= l.get(i, k) * l.get(j, k);
        }
        final double diagonal = l.get(j, j);
        if (abs(diagonal) > MathTools.EPSILON)
          val /= diagonal;
        else
          val = abs(val) < EPSILON ? 0 : (val * diagonal > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
        l.set(i, j, val);
        sum2 += val * val;
      }
      double diagonal = a.get(i, i) - sum2;
      if (diagonal < 0)
        throw new IllegalArgumentException("Matrix must be positive definite!");
      if (abs(diagonal) < EPSILON)
        diagonal = 0.;
      l.set(i, i, sqrt(diagonal));
    }
    return l;
  }

  public static Mx inverseLTriangle(final Mx a) {
    final int dim = a.rows();
    if (a.columns() != dim)
      throw new IllegalArgumentException("Matrix must be square for inverse!");
    final Mx inverse = new VecBasedMx(dim, a.columns());
    for (int j = 0; j < dim; j++) {
      if (a.get(j, j) != 0)
        inverse.set(j, j, 1. / a.get(j, j));
      else
        inverse.set(j, j, 0);
    }
    for (int j = 0; j < dim; j++) {
      for (int i = j + 1; i < dim; i++) {
        double sum = 0.0;
        for (int k = j; k < i; k++) {
          sum -= a.get(i, k) * inverse.get(k, j);
        }
        inverse.set(i, j, sum * inverse.get(i, i));
      }
    }
    return inverse;
  }

  public static Vec solveSystemLq(final Mx a, final Vec b) {
    final int dim = a.rows();
    if (a.columns() != dim)
      throw new IllegalArgumentException("Matrix must be square for inverse!");

    final Mx l = new VecBasedMx(dim, dim);
    final Mx q = new VecBasedMx(dim, dim);
    householderLQ(a, l, q);
    return multiply(multiply(q, inverseLTriangle(l)), b);
  }

  public static Mx E(final int dim) {
    final Mx result;
    if (dim > 10000)
      result = new VecBasedMx(dim, new SparseVec(dim * dim, dim));
    else
      result = new VecBasedMx(dim, dim);
    for (int i = 0; i < dim; i++)
      result.set(i, i, 1);
    return result;
  }

  public static Mx sparseE(final int dim) {
    final Mx result = new VecBasedMx(dim, new SparseVec(dim * dim));
    for (int i = 0; i < dim; i++)
      result.set(i, i, 1);
    return result;
  }

  public static void adjust(final Mx a, final Mx b) {
    if (a.columns() != b.columns())
      throw new IllegalArgumentException("Matrices must have a.columns == b.columns!");

    if (a.rows() != b.rows())
      throw new IllegalArgumentException("Matrices must have a.rows == b.rows!");

    for (int i = 0; i < a.rows(); ++i) {
      for (int j = 0; j < a.columns(); ++j) {
        a.adjust(i, j, b.get(i, j));
      }
    }
  }

  public static Mx multiply(final Mx a, final Mx b) {
    if (a.columns() != b.rows())
      throw new IllegalArgumentException("Matrices must have a.columns == b.rows!");
    final int dim = b.columns() * a.rows();
    final VecBasedMx result = new VecBasedMx(b.columns(), b.vec() instanceof SparseVec ? new SparseVec(dim) : new ArrayVec(dim));
    return multiplyTo(a, b, result);
  }

  public static Mx multiplyTo(Mx a, Mx b, Mx result) {
    final int dim = a.columns();
    final int rows = a.rows();
    for (int i = 0; i < rows; i++) {
      final Vec arow = a.row(i);
      final Vec resultRow = result.row(i);
      for (int t = 0; t < dim; t++) {
        final double scale = arow.get(t);
        if (abs(scale) > EPSILON)
          VecTools.incscale(resultRow, b.row(t), scale);
      }
    }
    return result;
  }

  public static double trace(final Mx a) {
    double result = 0.0;
    for (int i = 0; i < a.rows(); i++) {
      result += a.get(i, i);
    }
    return result;
  }

  public static Mx transpose(final Mx a) {
    if (a instanceof SparseMx) {
      final SparseMx result = new SparseMx(a.columns(), a.rows());

      for (int j = 0; j < a.rows(); j++) {
        final VecIterator nz = a.row(j).nonZeroes();
        while (nz.advance()) {
          final Vec row = result.row(nz.index());
          row.set(j, nz.value());
        }
      }
      return result;
    } else {
      final Mx result = new VecBasedMx(a.columns(), a.rows());
      for (int i = 0; i < a.rows(); i++) {
        for (int j = 0; j < a.columns(); j++)
          result.set(j, i, a.get(i, j));
      }
      return result;
    }
  }

  public static Mx transposeIt(final Mx a) {
    final int rows = a.rows();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < i; j++) {
        final double save = a.get(i, j);
        a.set(i, j, a.get(j, i));
        a.set(j, i, save);
      }
    }
    return a;
  }

  public static Vec multiply(final Mx mx, final Vec vec) {
    final Vec result = new ArrayVec(mx.rows());
    multiplyTo(mx, vec, result);
    return result;
  }

  public static Vec multiplyTo(final Mx mx, final Vec vec, final Vec result) {
    final int rows = mx.rows();
    if (rows != result.dim())
      throw new IllegalArgumentException();
    for (int i = 0; i < rows; i++) {
      result.set(i, VecTools.multiply(mx.row(i), vec));
    }
    return result;
  }

  public static Mx mahalanobis(final List<Vec> pool) {
    final int dim = pool.get(0).dim();
    final Vec mean = new ArrayVec(dim);
    final Mx covar = new VecBasedMx(dim, dim);
    for (int i = 0; i < pool.size(); i++) {
      VecTools.append(mean, pool.get(i));
    }
    scale(mean, -1. / pool.size());
    final Vec temp = new ArrayVec(dim);
    for (final Vec vec : pool) {
      VecTools.assign(temp, vec);
      VecTools.append(temp, mean);
      VecTools.addOuter(covar, temp, temp);
    }
    scale(covar, 1. / pool.size());
    final Mx l = choleskyDecomposition(covar);
    return inverseLTriangle(l);
  }

  public static void householderLQ(final Mx A, final Mx L, final Mx Q) {
    final int cols = A.columns();
    final int rows = A.rows();
    VecTools.assign(L, A);
    if (Q != null) {
      scale(Q, 0.);
      for (int i = 0; i < cols; i++)
        Q.set(i, i, 1.);
    }
    final Vec hhplane = new ArrayVec(cols);
    for (int i = 0; i < cols - 1; i++) {
      if (i > 0)
        hhplane.set(i - 1, 0);
      double diag2 = 0;
      for (int j = i; j < cols; j++) {
        final double Lij = L.get(i, j);
        diag2 += Lij * Lij;
      }
      if (diag2 < EPSILON)
        continue;
      final double origDiag = L.get(i, i);
      final double diag = origDiag > 0 ? -sqrt(diag2) : sqrt(diag2);
      final double r = 2 * sqrt(0.5 * (diag2 - diag * origDiag));
      hhplane.set(i, (origDiag - diag) / r);
      for (int j = i + 1; j < cols; j++) {
        hhplane.set(j, L.get(i, j) / r);
      }
      L.set(i, i, diag);
      for (int j = i + 1; j < cols; j++) {
        L.set(i, j, 0.);
      }
      for (int k = i + 1; k < rows; k++) {
        final Vec lSub = L.row(k).sub(i, cols - i);
        final Vec hhplaneSub = hhplane.sub(i, cols - i);
        double product = -2 * VecTools.multiply(lSub, hhplaneSub);
        VecTools.incscale(lSub, hhplaneSub, product);
      }
      if (Q != null) {
        for (int j = 0; j < rows; j++) {
          final Vec qSub = Q.row(j).sub(i, cols - i);
          final Vec hhplaneSub = hhplane.sub(i, cols - i);
          double product = -2 * VecTools.multiply(qSub, hhplaneSub);
          VecTools.incscale(qSub, hhplaneSub, product);
        }
      }
    }
  }

  public static void eigenDecomposition(final Mx mx, final Mx sigma, final Mx q) {
    Mx similar = mx;
    Mx joinedInvertedTransform = E(mx.columns());
    final Mx trans = new VecBasedMx(mx.columns(), mx.columns() > 10000 ? new SparseVec(mx.dim()) : new ArrayVec(mx.dim()));

    for (int i = 0; i < 2000; i++) {
      double nonDiagonalWeight = nonDiagonalWeight(similar);
//      System.out.println("Iteration " + i + ": " + nonDiagonalWeight);
      if (nonDiagonalWeight < EPSILON * similar.columns())
        break;
      householderLQ(similar, sigma, trans);
//      System.out.println("LQ dist: " + distance(multiply(sigma, trans), similar));
      transposeIt(trans);
      similar = multiply(trans, sigma);
      joinedInvertedTransform = multiply(trans, joinedInvertedTransform);
//      System.out.println(distance(multiply(joinedInvertedTransform, multiply(mx, transpose(joinedInvertedTransform))), similar));
    }

    VecTools.assign(sigma, similar);
    VecTools.assign(q, joinedInvertedTransform);

    final MxIterator mxIterator = sigma.nonZeroes();
    while (mxIterator.advance()) {
      if (mxIterator.row() != mxIterator.column())
        mxIterator.setValue(0);
    }
  }

  public static void lanczos(final Mx mx, final Mx q, final Mx trisigma, FastRandom rng) {
    Vec w;
    VecTools.fill(q, 0);
    VecTools.fill(trisigma, 0);
    {
      final Vec v = q.row(0);
      { // initialize random vector
        VecTools.fillGaussian(v, rng);
        VecTools.normalizeL2(v);
      }

      w = multiply(mx, v);
      final double alpha = VecTools.multiply(v, w);
      trisigma.set(0, 0, alpha);
      incscale(w, v, -alpha);
    }
    for (int j = 1; j < mx.columns(); j++) {
      final Vec v = q.row(j);

      final double beta = VecTools.norm(w);
      trisigma.set(j - 1, j, beta);
      trisigma.set(j, j - 1, beta);

      if (Math.abs(beta) <= MathTools.EPSILON) {
        VecTools.fillGaussian(v, rng);
      } else VecTools.incscale(v, w, 1. / beta);

      if (rng.nextDouble() < 0.1) { // restore ortonormality
        for (int k = j - 1; k >= 0; k--) {
          VecTools.incscale(v, q.row(k), -VecTools.multiply(q.row(k), v));
        }
        VecTools.normalizeL2(v);
      }

      w = multiply(mx, v);
      final double alpha = VecTools.multiply(v, w);
      trisigma.set(j, j, alpha);
      incscale(w, q.row(j - 1), -beta);
      incscale(w, v, -alpha);
    }
  }

  public static void divideAndConquer(Mx trisigma, Mx sigma, Mx q) {
    fill(sigma, 0);
    fill(q, 0);
    divideAndConquerInner(trisigma, sigma, q);
  }

  private static void divideAndConquerInner(Mx trisigma, Mx sigma, Mx q) {
    int dim = trisigma.columns();
    if (dim == 1) {
      sigma.set(0, 0, trisigma.get(0, 0));
      q.set(0, 0, 1);
      return;
    }
    final int div = dim / 2;
    final double beta = trisigma.get(div - 1, div);
    trisigma.set(div - 1, div, 0);
    trisigma.set(div, div - 1, 0);
    trisigma.adjust(div - 1, div - 1, -beta);
    trisigma.adjust(div, div, -beta);
    divideAndConquerInner(
            trisigma.sub(0, 0, div, div),
            sigma.sub(0, 0, div, div),
            q.sub(0, 0, div, div)
    );
    divideAndConquerInner(
            trisigma.sub(div, div, dim - div, dim - div),
            sigma.sub(div, div, dim - div, dim - div),
            q.sub(div, div, dim - div, dim - div)
    );

    final Mx resultQ = new VecBasedMx(dim, dim);

    trisigma.set(div - 1, div, beta);
    trisigma.set(div, div - 1, beta);
    trisigma.adjust(div - 1, div - 1, beta);
    trisigma.adjust(div, div, beta);

    final Vec u = new ArrayVec(dim);
    assign(u.sub(0, div), q.col(div - 1).sub(0, div));
    assign(u.sub(div, dim - div), q.col(div).sub(div, dim - div));
    {
//      Vec v = new ArrayVec(dim);
//      v.set(div - 1, 1);
//      v.set(div, 1);
//      System.out.println("u: " + u + " real u: " + (u = multiply(q, v)));
    }
    Mx m = outer(u, u);
    scale(m, beta);
    append(m, sigma);
//    System.out.println("Q^T (\\Sigma + \\beta uu^T) Q: " + multiply(transpose(q), multiply(m, q)) + "\ntrisigma: " + trisigma);

    final Vec d = new ArrayVec(dim);
    final Mx qInner = new VecBasedMx(dim, dim);
    for (int i = 0; i < dim; i++) {
      d.set(i, sigma.get(i, i));
    }

    int[] order = ArrayTools.sequence(0, dim);
    double[] dArr = d.toArray();
    ArrayTools.parallelSort(dArr, order);
    int[] rorder = new int[order.length];
    for (int i = 0; i < dim; i++)
      rorder[order[i]] = i;

    final double[] roots = new double[2];
    for (int i = 0; i < dim; i++) {
      final int index = order[i];
      final double eigenVal;
      final Vec eigenVec = qInner.row(i);
      final double d_i = d.get(index);
      final double u_i = u.get(index);
      double d_i1 = Double.POSITIVE_INFINITY;
      double u_i1 = 0;
      for (int j = i + 1; j < dim; j++) {
        if (abs(u.get(order[j])) > MathTools.EPSILON) {
          d_i1 = d.get(order[j]);
          u_i1 = u.get(order[j]);
          break;
        }
      }
      final SecularFunction secularFunction = new SecularFunction(d, beta, u);
      if (!MathTools.locality(d_i, d_i1, 1e-6) && abs(u_i) > 1e-6) {
        double x;
        if (Double.isFinite(d_i1)) { // quadratic approx
          x = (d_i + d_i1) / 2;
          double sumRight;
          double sumLeft;
          int it = 0;
          while (true) {
            double sumDotLeft = 0;
            double sumDotRight = 0;
            sumLeft = 0;
            sumRight = 0;

            for (int t = 0; t < dim; t++) {
              if (rorder[t] <= i) {
                sumLeft += beta * u.get(t) * u.get(t) / (d.get(t) - x);
                sumDotLeft += beta * u.get(t) * u.get(t) / MathTools.sqr(d.get(t) - x);
              } else {
                sumRight += beta * u.get(t) * u.get(t) / (d.get(t) - x);
                sumDotRight += beta * u.get(t) * u.get(t) / MathTools.sqr(d.get(t) - x);
              }
            }

            double c1 = sumDotLeft * MathTools.sqr(d_i - x);
            double c2 = sumDotRight * MathTools.sqr(d_i1 - x);
            double c3 = 1 + sumLeft + sumRight - sumDotLeft * (d_i - x) - sumDotRight * (d_i1 - x);

            MathTools.quadratic(roots,
                    c3,
                    -(c1 + c2 + c3 * (d_i + d_i1)),
                    c1 * d_i1 + c2 * d_i + c3 * d_i * d_i1
            );
            double nextX = roots[0] < d_i1 && roots[0] > d_i ? roots[0] : roots[1];
            if (Math.abs(1 + sumLeft + sumRight) < 1e-6)
              break;
            else if (nextX == x || it++ > 100) {
              final double left = MathTools.inc(d_i);
              final double right = MathTools.dec(d_i1);
              x = MathTools.bisection(secularFunction, left, right);
              break;
            }
            x = nextX;
          }
        } else { // linear approx f(x) \sim c_2 + \frac{c_1}{d_i - x}
          x = d_i + 100;
          double sum;
          int it = 0;
          while (true) {
            double sumDot = 0;
            sum = 0;

            for (int t = 0; t < dim; t++) {
              sum += beta * u.get(t) * u.get(t) / (d.get(t) - x);
              sumDot += beta * u.get(t) * u.get(t) / MathTools.sqr(d.get(t) - x);
            }

            final double c1 = sumDot * MathTools.sqr(d_i - x);
            final double c2 = 1 + sum - sumDot * (d_i - x);
            final double nextX = d_i + c1 / c2;
            if (Math.abs(1 + sum) < MathTools.EPSILON) {
              break;
            } else if (nextX == x || it++ > 10000 || nextX <= d_i) {
              break;
            }
            x = nextX;
          }
        }
        if (Math.abs(secularFunction.value(x)) > 1e-2) {
          log.fine("Unable to eliminate secular function error: " + Math.abs(secularFunction.value(x)));
        }
        eigenVal = x;
        for (int k = 0; k < dim; k++) {
          double val = u.get(k) / (d.get(k) - eigenVal);
          eigenVec.set(k, val);
        }
        VecTools.normalizeL2(eigenVec);
      } else {
        eigenVal = d_i;
        eigenVec.set(index, 1);
      }
      sigma.set(i, i, eigenVal);
    }
    multiplyTo(qInner, q, resultQ);
    assign(q, resultQ);
//
//    {
//      final Mx result = MxTools.multiply(transpose(q), MxTools.multiply(sigma, q));
//      if (distance(trisigma, result) > 0.01) {
//        System.out.println(/*"" + trisigma + "\n" + result + "\n" */ "distance on merge" + distance(trisigma, result));
//        divideAndConquer(trisigma, sigma, q);
//      }
//      for (int i = 0; i < dim; i++) {
//        final Vec resultEigenVec = resultQ.row(i);
//        final double eigenVal = sigma.get(i, i);
//        Vec incscale = incscale(multiply(trisigma, resultEigenVec), resultEigenVec, -eigenVal);
//        if (norm(incscale) > 0.001)
//          System.out.println("Eigen value: " + eigenVal + " vec: " + resultEigenVec + " Ax - \\lamda x: " + incscale);
//      }
//    }
  }

  public static double nonTriangularWeight(final Mx mx) {
    double lower = 0;
    double upper = 0;
    final MxIterator mxIterator = mx.nonZeroes();
    while (mxIterator.advance()) {
      if (mxIterator.row() > mxIterator.column())
        upper += mxIterator.value() * mxIterator.value();
      if (mxIterator.row() < mxIterator.column())
        lower += mxIterator.value() * mxIterator.value();
    }

    return Math.sqrt(Math.max(lower, upper));
  }

  public static Mx inverseCholesky(final Mx a) {
    final Mx l = choleskyDecomposition(a);
    final Mx inverseL = inverseLTriangle(l);
    return multiply(transpose(inverseL), inverseL);
  }

  public static Vec solveSystemCholesky(final Mx a, Vec b) {
    return multiply(inverseCholesky(a), b);
  }

  public static String prettyPrint(final Mx mx) {
    final StringBuilder builder = new StringBuilder();
    for (int i = 0; i < mx.rows(); i++) {
      for (int j = 0; j < mx.columns(); j++) {
        if (j > 0)
          builder.append(' ');
        builder.append(String.format(Locale.ENGLISH, "%.2f", mx.get(i, j)));
      }
      builder.append('\n');
    }
    return builder.toString();
  }

  public static Vec[] splitMxColumns(final Mx mx) {
    final Vec[] columns = new Vec[mx.columns()];
    for (int i = 0; i < mx.columns(); i++) {
      columns[i] = new ArrayVec(mx.col(i).toArray());
    }
    return columns;
  }

  public static Mx normalize(final Mx ds, final NormalizationType type, final NormalizationProperties props) {
    final Vec mean = new ArrayVec(ds.columns());
    final Mx covar = new VecBasedMx(ds.columns(), ds.columns());
    double targetMean;
    double targetVar;
    final Mx trans;
    final Vec temp = new ArrayVec(ds.columns());
    for (int i = 0; i < ds.rows(); i++) {
      final Vec vec = ds.row(i);
      VecTools.assign(temp, vec);
      VecTools.append(temp, mean);
      VecTools.addOuter(covar, temp, temp);
    }
    scale(covar, 1. / ds.rows());
    switch (type) {
      case SPHERE:
        final Mx l = choleskyDecomposition(covar);
        trans = inverseLTriangle(l);
        break;
      case PCA:
        trans = new VecBasedMx(ds.columns(), ds.columns());
        eigenDecomposition(covar, trans, new VecBasedMx(ds.columns(), ds.columns()));
        break;
      case SCALE:
        trans = new VecBasedMx(ds.columns(), ds.columns());
        for (int i = 0; i < trans.columns(); i++) {
          trans.set(i, i, 1. / Math.sqrt(covar.get(i, i)));
        }
        break;
      default:
        throw new UnsupportedOperationException();
    }
    final Mx normalized = VecTools.copy(ds);
    for (int i = 0; i < ds.rows(); i++) {
      final Vec row = normalized.row(i);
      VecTools.append(row, mean);
      VecTools.assign(row, multiply(trans, row));
    }
    props.xMean = mean;
    props.xTrans = trans;
    return normalized;
  }

  public static double nonDiagonalWeight(final Mx mx) {
    double lower = 0;
    double upper = 0;
    final MxIterator mxIterator = mx.nonZeroes();
    while (mxIterator.advance()) {
      if (mxIterator.row() > mxIterator.column())
        upper += mxIterator.value() * mxIterator.value();
      if (mxIterator.row() < mxIterator.column())
        lower += mxIterator.value() * mxIterator.value();
    }

    return Math.sqrt(lower + upper);
  }

  public static Vec solveSystemGaussZeildel(Mx a, Vec b) {
    return solveGaussZeildel(a, b, MathTools.EPSILON);
  }

  //Converge if a is symmetric positive-definite or
  //a is strictly or irreducibly diagonally dominant
  public static Vec solveGaussZeildel(Mx a, Vec b, double stopCondition) {
    final int N = b.dim();
    Vec x0 = new ArrayVec(N);
    Vec x1 = new ArrayVec(N);
    int iterations = 0;
    do {
      iterations++;
      for (int i = 0; i < N; i++) {
        double value = b.get(i);
        final VecIterator iterator = a.row(i).nonZeroes();
        while (iterator.advance()) {
          final int index = iterator.index();
          if (index < i) {
            value -= iterator.value() * x1.get(index);
          }
          if (index > i) {
            value -= iterator.value() * x0.get(index);
          }
        }
        if (abs(a.get(i, i)) > MathTools.EPSILON) {
          x1.set(i, value / a.get(i, i));
        } else if (Math.abs(value) < MathTools.EPSILON) {
          x1.set(i, 0);
        } else {
          throw new InvalidParameterException("Matrix must have non-zero diagonal elements");
        }
      }

      {
        Vec temp = x0;
        x0 = x1;
        x1 = temp;
      }
    } while (distance(x0, x1) / x0.dim() > stopCondition);
    return x1;
  }

  public enum NormalizationType {
    SPHERE,
    PCA,
    SCALE
  }

  public static class NormalizationProperties {
    public Vec xMean;
    public Mx xTrans;
  }

  private static class SecularFunction extends AnalyticFunc.Stub {
    private final Vec d;
    private final double beta;
    private final Vec u;

    public SecularFunction(Vec d, double beta, Vec u) {
      this.d = d;
      this.beta = beta;
      this.u = u;
    }

    @Override
    public double value(double x) {
      double value = 1;
      for (int i = 0; i < d.dim(); i++) {
        value += beta * u.get(i) * u.get(i) / (d.get(i) - x);
      }
      return value;
    }

    @Override
    public double gradient(double x) {
      double value = 0;
      for (int i = 0; i < d.dim(); i++) {
        value += beta * u.get(i) * u.get(i) / MathTools.sqr(d.get(i) - x);
      }
      return value;
    }
  }
}
