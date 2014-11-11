package com.spbsu.commons.math.vectors;

import com.spbsu.commons.math.vectors.impl.mx.VecBasedMx;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.math.vectors.impl.vectors.SparseVec;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Locale;

import static java.lang.Math.sqrt;

/**
 * User: qdeee
 * Date: 05.06.14
 */
public class MxTools {
  private static final double EPSILON = 1e-5;

  public static boolean checkSymmetry(Mx a) {
    if (a.columns() != a.rows())
      return false;
    int dim = a.columns();
    for (int i = 0; i < dim; i++)
      for (int j = 0; j < dim; j++)
        if (Math.abs(a.get(i, j) - a.get(j, i)) > EPSILON)
          return false;
    return true;
  }

  public static Vec rowSum(Mx a) {
    Vec result = new ArrayVec(a.rows());
    for (int row = 0; row < a.rows(); ++row) {
      double val = 0;
      for (int col = 0; col < a.columns(); ++col) {
        val += a.get(row, col);
      }
      result.set(row, val);
    }
    return result;
  }

  public static Mx inverse(Mx A) {
    Mx L = new VecBasedMx(A.rows(), A.columns());
    Mx Q = new VecBasedMx(A.rows(), A.columns());
    householderLQ(A, L, Q);
    L = inverseLTriangle(L);
    final Mx Inv = MxTools.multiply(Q, L);
    return Inv;
  }

  public static Mx laplacian(Mx a) {
    Vec d = rowSum(a);
    Mx L = new VecBasedMx(a.rows(), a.columns());
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

  public static Mx choleskyDecomposition(Mx a) {
    if (a.columns() != a.rows())
      throw new IllegalArgumentException("Matrix must be square for Cholesky decomposition!");
    if (!checkSymmetry(a))
      throw new IllegalArgumentException("Matrix must be symmetric!");
    Mx l = new VecBasedMx(a.columns(), a.columns());
    // Cholesky–Banachiewicz schema
    for (int i = 0; i < a.rows(); i++) {
      double sum2 = 0;
      for (int j = 0; j < i; j++) {
        double val = a.get(i, j);
        for (int k = 0; k < j; k++) {
          val -= l.get(i, k) * l.get(j, k);
        }
        val /= val != 0 ? l.get(j, j) : 1;
        l.set(i, j, val != 0 ? val : 0);
        sum2 += val * val;
      }
      double diagonal = a.get(i, i) - sum2;
      if (diagonal < 0)
        throw new IllegalArgumentException("Matrix must be positive definite!");
      l.set(i, i, sqrt(diagonal));
    }
    return l;
  }

  public static Mx inverseLTriangle(Mx a) {
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

  public static Mx E(int dim) {
    final Mx result = new VecBasedMx(dim, dim);
    for (int i = 0; i < dim; i++)
      result.set(i, i, 1);
    return result;
  }

  public static Mx sparseE(int dim) {
    final Mx result = new VecBasedMx(dim, new SparseVec(dim * dim));
    for (int i = 0; i < dim; i++)
      result.set(i, i, 1);
    return result;
  }

  public static Mx multiply(Mx a, Mx b) {
    final int dim = a.columns();
    if (dim != b.rows())
      throw new IllegalArgumentException("Matrices must have a.columns == b.rows!");
    final int rows = a.rows();
    final int columns = b.columns();
    final VecBasedMx result = new VecBasedMx(rows, columns);
    for (int i = 0; i < rows; i++) {
      final Vec arow = a.row(i);
      final Vec resultRow = result.row(i);
      for (int t = 0; t < dim; t++) {
        final double scale = arow.get(t);
        if (Math.abs(scale) > EPSILON)
          VecTools.incscale(resultRow, b.row(t), scale);
      }
    }
    return result;
  }

  public static double trace(Mx a) {
    double result = 0.0;
    for (int i = 0; i < a.rows(); i++) {
      result += a.get(i, i);
    }
    return result;
  }

  public static Mx transpose(Mx a) {
    final Mx result = new VecBasedMx(a.columns(), a.rows());
    for (int i = 0; i < a.rows(); i++) {
      for (int j = 0; j < a.columns(); j++)
        result.set(j, i, a.get(i, j));
    }
    return result;
  }

  public static Mx transposeIt(Mx a) {
    final int rows = a.rows();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < i; j++) {
        double save = a.get(i, j);
        a.set(i, j, a.get(j, i));
        a.set(j, i, save);
      }
    }
    return a;
  }

  public static Vec multiply(Mx mx, Vec vec) {
    if (vec instanceof Mx)
      return multiply(mx, (Mx) vec);
    return multiply(mx, new VecBasedMx(1, vec));
  }

  public static Mx mahalanobis(List<Vec> pool) {
    final int dim = pool.get(0).dim();
    Vec mean = new ArrayVec(dim);
    Mx covar = new VecBasedMx(dim, dim);
    for (int i = 0; i < pool.size(); i++) {
      VecTools.append(mean, pool.get(i));
    }
    VecTools.scale(mean, -1. / pool.size());
    Vec temp = new ArrayVec(dim);
    for (Vec vec : pool) {
      VecTools.assign(temp, vec);
      VecTools.append(temp, mean);
      VecTools.addOuter(covar, temp, temp);
    }
    VecTools.scale(covar, 1. / pool.size());
    final Mx l = choleskyDecomposition(covar);
    return inverseLTriangle(l);
  }

  public static void householderLQ(Mx A, Mx L, Mx Q) {
    final int cols = A.columns();
    final int rows = A.rows();
    VecTools.assign(L, A);
    if (Q != null) {
      VecTools.scale(Q, 0.);
      for (int i = 0; i < cols; i++)
        Q.set(i, i, 1.);
    }
    Vec hhplane = new ArrayVec(cols);
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
      double diag = origDiag > 0 ? -sqrt(diag2) : sqrt(diag2);
      double r = 2 * sqrt(0.5 * (diag2 - diag * origDiag));
      hhplane.set(i, (origDiag - diag) / r);
      for (int j = i + 1; j < cols; j++) {
        hhplane.set(j, L.get(i, j) / r);
      }
      L.set(i, i, diag);
      for (int j = i + 1; j < cols; j++) {
        L.set(i, j, 0.);
      }
      for (int k = i + 1; k < rows; k++) {
        double product = 0.;
        for (int j = i; j < cols; j++)
          product += L.get(k, j) * hhplane.get(j);
        product *= -2.;
        for (int j = i; j < cols; j++)
          L.adjust(k, j, hhplane.get(j) * product);
      }
      if (Q != null) {
        for (int j = 0; j < cols; j++) {
          double product = 0.;
          for (int k = i; k < cols; k++)
            product += Q.get(j, k) * hhplane.get(k);
          product *= -2.;

          for (int k = i; k < cols; k++)
            Q.adjust(j, k, product * hhplane.get(k));
        }
      }
    }
  }

  public static void eigenDecomposition(Mx mx, Mx q, Mx sigma) {
    Mx similar = mx;
    Mx joinedInvertedTransform = E(mx.columns());
    Mx trans = new VecBasedMx(mx.columns(), new ArrayVec(mx.dim()));

    for (int i = 0; i < 100 && nonTriangularWeight(similar) > EPSILON * similar.dim(); i++) {
      householderLQ(similar, sigma, trans);
      transposeIt(trans);
      joinedInvertedTransform = multiply(trans, joinedInvertedTransform);
      similar = multiply(trans, sigma);
//      System.out.println(distance(multiply(joinedInvertedTransform, multiply(mx, transpose(joinedInvertedTransform))), similar));
    }

    VecTools.assign(sigma, similar);
    VecTools.assign(q, joinedInvertedTransform);

    MxIterator mxIterator = sigma.nonZeroes();
    while (mxIterator.advance()) {
      if (mxIterator.row() != mxIterator.column())
        mxIterator.setValue(0);
    }
  }

  public static double nonTriangularWeight(Mx mx) {
    double lower = 0;
    double upper = 0;
    MxIterator mxIterator = mx.nonZeroes();
    while (mxIterator.advance()) {
      if (mxIterator.row() > mxIterator.column())
        upper += mxIterator.value() * mxIterator.value();
      if (mxIterator.row() < mxIterator.column())
        lower += mxIterator.value() * mxIterator.value();
    }

    return Math.sqrt(Math.max(lower, upper));
  }

  public static Mx inverseCholesky(Mx a) {
    final Mx l = choleskyDecomposition(a);
    final Mx inverseL = inverseLTriangle(l);
    return multiply(transpose(inverseL), inverseL);
  }

  public static String prettyPrint(Mx mx) {
    StringBuilder builder = new StringBuilder();
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

  public static Vec[] splitMxColumns(Mx mx) {
    final Vec[] columns = new Vec[mx.columns()];
    for (int i = 0; i < mx.columns(); i++) {
      columns[i] = new ArrayVec(mx.col(i).toArray());
    }
    return columns;
  }

  public static Mx normalize(Mx ds, NormalizationType type, NormalizationProperties props) {
    final Vec mean = new ArrayVec(ds.columns());
    final Mx covar = new VecBasedMx(ds.columns(), ds.columns());
    double targetMean;
    double targetVar;
    Mx trans;
    Vec temp = new ArrayVec(ds.columns());
    for (int i = 0; i < ds.rows(); i++) {
      Vec vec = ds.row(i);
      VecTools.assign(temp, vec);
      VecTools.append(temp, mean);
      VecTools.addOuter(covar, temp, temp);
    }
    VecTools.scale(covar, 1. / ds.rows());
    switch (type) {
      case SPHERE:
        final Mx l = choleskyDecomposition(covar);
        trans = inverseLTriangle(l);
        break;
      case PCA:
        trans = new VecBasedMx(ds.columns(), ds.columns());
        eigenDecomposition(covar, new VecBasedMx(ds.columns(), ds.columns()), trans);
        break;
      case SCALE:
        trans = new VecBasedMx(ds.columns(), ds.columns());
        for (int i = 0; i < trans.columns(); i++) {
          trans.set(i, i, 1. / Math.sqrt(covar.get(i, i)));
        }
        break;
      default:
        throw new NotImplementedException();
    }
    Mx normalized = VecTools.copy(ds);
    for (int i = 0; i < ds.rows(); i++) {
      Vec row = normalized.row(i);
      VecTools.append(row, mean);
      VecTools.assign(row, multiply(trans, row));
    }
    props.xMean = mean;
    props.xTrans = trans;
    return normalized;
  }

  public static double nonDiagonalWeight(Mx mx) {
    double lower = 0;
    double upper = 0;
    MxIterator mxIterator = mx.nonZeroes();
    while (mxIterator.advance()) {
      if (mxIterator.row() > mxIterator.column())
        upper += mxIterator.value() * mxIterator.value();
      if (mxIterator.row() < mxIterator.column())
        lower += mxIterator.value() * mxIterator.value();
    }

    return Math.sqrt(lower + upper);
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
}
