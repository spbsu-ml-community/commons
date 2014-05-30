package com.spbsu.commons.math.vectors;

import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.math.vectors.impl.ArrayVec;
import com.spbsu.commons.math.vectors.impl.SparseVec;
import com.spbsu.commons.math.vectors.impl.VecBasedMx;
import com.spbsu.commons.util.ArrayTools;
import com.spbsu.commons.util.RBTreeNode;
import com.spbsu.commons.util.RBTreeNodeBase;
import com.spbsu.commons.util.RedBlackTree;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.lang.Math.log;
import static java.lang.Math.sqrt;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:28:37
 */
@SuppressWarnings("UnusedDeclaration")
public class VecTools {
  private static final double EPSILON = 1e-5;

  public static int hashCode(Vec v) {
    int hashCode = 0;
    final VecIterator iter = v.nonZeroes();
    while (iter.advance()) {
      hashCode <<= 1;
      hashCode += iter.index();
      hashCode += iter.value() * 10000;
    }
    return hashCode;
  }

  public static boolean equals(final Vec left, final Vec right) {
    if (left.dim() != right.dim())
      return false;
    if (left.getClass() == right.getClass()) {
      if (left instanceof SparseVec) {
        return ((SparseVec) left).indices.equals(((SparseVec) right).indices)
                && ((SparseVec) left).values.equals(((SparseVec) right).values);
      } else if (left instanceof ArrayVec) {
        final ArrayVec larray = (ArrayVec) left;
        final ArrayVec rarray = (ArrayVec) right;

        if (larray.array == rarray.array && larray.start == rarray.start && larray.length == rarray.length)
          return true;
      }
    }

    for (int i = 0; i < left.dim(); i++) {
      if (left.get(i) != right.get(i))
        return false;
    }

    return true;
  }

  public static <T extends Vec> T append(T leftOrig, final Vec... rest) {
    Vec left = leftOrig;
    if (left instanceof VecBasedMx)
      left = ((VecBasedMx)left).vec;

    for (Vec vec : rest) {
      if (left.dim() != vec.dim())
        throw new IllegalArgumentException("vectors dimensions differ");
      if (vec instanceof VecBasedMx)
        vec = ((VecBasedMx)vec).vec;
      if (left instanceof SparseVec) {
        final VecIterator iterRight = vec.nonZeroes();
        final int maxSize = countNonZeroesUpperBound(left) + countNonZeroesUpperBound(vec);
        TIntArrayList newIndeces = new TIntArrayList(maxSize);
        TDoubleArrayList newValues = new TDoubleArrayList(maxSize);
        final VecIterator iterLeft = left.nonZeroes();
        iterLeft.advance();
        iterRight.advance();

        while (iterLeft.isValid() || iterRight.isValid()) {
          if (iterLeft.isValid() && (!iterRight.isValid() || iterLeft.index() < iterRight.index())) {
            newIndeces.add(iterLeft.index());
            newValues.add(iterLeft.value());
            iterLeft.advance();
          }
          else if (iterLeft.isValid() && iterLeft.index() == iterRight.index()) {
            final double newVal = iterLeft.value() + iterRight.value();
            if (newVal != 0) {
              newIndeces.add(iterLeft.index());
              newValues.add(newVal);
            }
            iterLeft.advance();
            iterRight.advance();
          }
          else {
            newIndeces.add(iterRight.index());
            newValues.add(iterRight.value());
            iterRight.advance();
          }
        }
        ((SparseVec) left).indices = newIndeces;
        ((SparseVec) left).values = newValues;
      }
      else if (left instanceof ArrayVec && left.getClass().equals(vec.getClass())) {
        ((ArrayVec) left).add((ArrayVec) vec);
      }
      else {
        final VecIterator viter = vec.nonZeroes();
        while (viter.advance()) {
          left.adjust(viter.index(), viter.value());
        }
      }
    }

    return leftOrig;
  }

  public static double multiply(Vec left, Vec right) {
    if (left.dim() != right.dim())
      throw new IllegalArgumentException("Vector basises are not of the same size left:" + left.dim() + ", right: " + right.dim());
    if (left instanceof ArrayVec && right instanceof ArrayVec) {
      return ((ArrayVec) left).mul((ArrayVec) right);
    }
    final VecIterator liter = left.nonZeroes();
    final VecIterator riter = right.nonZeroes();
    return multiply(liter, riter);
  }

  private static double multiply(VecIterator liter, VecIterator riter) {
    double result = 0;
    if (!liter.advance() || !riter.advance())
      return 0;
    while (liter.isValid() && riter.isValid()) {
      int lindex = liter.index(), rindex = riter.index();
      if (lindex == rindex) {
        result += liter.value() * riter.value();
        liter.advance();
        riter.advance();
      }
      if (riter.isValid()) {
        while (lindex > riter.index() && riter.advance());
        if (riter.isValid() && liter.isValid()) {
          rindex = riter.index();
          while (rindex > liter.index() && liter.advance());
        }
      }
    }
    return result;
  }

  public static double distanceAV(Vec left, Vec right) {
    if (left.dim() != right.dim())
      throw new IllegalArgumentException("Vector basises are not the same");
    final int size = left.dim();
    double result = 0;
    for (int i = 0; i < size; i++) {
      final double lv = left.get(i);
      final double rv = right.get(i);
      result += (lv - rv) * (lv - rv);
    }
    return sqrt(result);
  }

  public static double distanceAVJS12(Vec left, Vec right) {
    if (left.dim() != right.dim())
      throw new IllegalArgumentException("Vector basises are not the same");
    final int size = left.dim();
    double result = 0;
    for (int i = 0; i < size; i++) {
      final double p = left.get(i);
      final double q = right.get(i);
      final double pr = p == 0 ? 0 : p * log(2 * p / (p + q));
      final double qr = q == 0 ? 0 : q * log(2 * q / (p + q));
      result += pr + qr;
    }
    return sqrt(result);
  }

  public static double distance(Vec left, Vec right) {
    if (left instanceof VecBasedMx)
      left = ((VecBasedMx)left).vec;
    if (right instanceof VecBasedMx)
      right = ((VecBasedMx)right).vec;

    if (left.dim() != right.dim())
      throw new IllegalArgumentException("Vector basises are not the same");
    if (left instanceof ArrayVec && right instanceof ArrayVec) {
      final ArrayVec larray = (ArrayVec) left;
      final ArrayVec rarray = (ArrayVec) right;
      return sqrt(larray.l2(rarray));
    }
    final VecIterator liter = left.nonZeroes();
    final VecIterator riter = right.nonZeroes();
    double result = 0;
    final boolean lStart = liter.advance();
    final boolean rStart = riter.advance();
    if (!lStart && !rStart)
      return 0;
    int lindex = lStart ? liter.index() : Integer.MAX_VALUE, rindex = rStart ? riter.index() : Integer.MAX_VALUE;
    while (liter.isValid() && riter.isValid()) {
      if (rindex == lindex && liter.isValid() && riter.isValid()) {
        result += (liter.value() - riter.value()) * (liter.value() - riter.value());
        if (liter.advance())
          lindex = liter.index();
        if (riter.advance())
          rindex = riter.index();
      }
      else if (lindex > rindex && riter.isValid()) {
        result += riter.value() * riter.value();
        if(riter.advance())
          rindex = riter.index();
      }
      else if (liter.isValid()) {
        result += liter.value() * liter.value();
        if(liter.advance())
          lindex = liter.index();
      }
    }
    return sqrt(result);
  }

  public static double distanceJS12(Vec left, Vec right) {
    if (left.dim() != right.dim())
      throw new IllegalArgumentException("Vector basises are not the same");
    final VecIterator liter = left.nonZeroes();
    final VecIterator riter = right.nonZeroes();
    double result = 0;
    if (!liter.advance() || !riter.advance())
      return 0;
    int lindex = liter.index(), rindex = riter.index();
    while (liter.isValid() && riter.isValid()) {
      if (rindex == lindex) {
        final double p = liter.value();
        final double q = riter.value();
        result += p * log(2 * p / (p + q)) + q * log(2 * q / (p + q));
        if (liter.advance())
          lindex = liter.index();
        if (riter.advance())
          rindex = riter.index();
      }
      else if (lindex > rindex) {
        result += riter.value() * log(2);
        if(riter.advance())
          rindex = riter.index();
      }
      else {
        result += liter.value() * log(2);
        if(liter.advance())
          lindex = liter.index();
      }
    }
    return sqrt(result);
  }

  public static SparseVec copySparse(Vec vec) {
    SparseVec copy;
    if (vec instanceof SparseVec)
      copy = new SparseVec(((SparseVec)vec).basis());
    else
      copy = new SparseVec(new IntBasis(vec.dim()));
    append(copy, vec);
    return copy;
  }

  public static double l1(Vec vec) {
    double result = 0;
    final VecIterator iterator = vec.nonZeroes();
    while (iterator.advance()) {
      result += Math.abs(iterator.value());
    }
    return result;
  }

  public static void scale(Vec vec, Vec scale) {
    final VecIterator iterator = vec.nonZeroes();
    while (iterator.advance()) {
      iterator.setValue(iterator.value() * scale.get(iterator.index()));
    }
  }

  public static void outer(Mx result, Vec a, Vec b) {
    if (a.dim() != b.dim())
      throw new IllegalArgumentException("Vector dimensions must be equal for outer product");
    {
      final VecIterator itA = a.nonZeroes();
      while (itA.advance()) {
        final int rowStart = itA.index() * a.dim();
        final VecIterator itB = b.nonZeroes();
        while (itB.advance()) {
          result.set(rowStart + itB.index(), itB.value() * itA.value());
        }
      }
    }
  }

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

  public static void addOuter(Mx result, Vec a, Vec b) {
    if (a.dim() != result.rows() || b.dim() != result.columns())
      throw new IllegalArgumentException("Vector dimensions must be equal for outer product");
    {
      final VecIterator itA = a.nonZeroes();
      while (itA.advance()) {
        final int i = itA.index();
        final VecIterator itB = b.nonZeroes();
        while (itB.advance()) {
          final int j = itB.index();
          result.adjust(i, j, itB.value() * itA.value());
        }
      }
    }
  }

  public static Mx outer(Vec a, Vec b) {
    if (a.dim() != b.dim())
      throw new IllegalArgumentException("Vector dimensions must be equal for outer product");
    final VecBasedMx result = new VecBasedMx(a.dim(), b.dim());
    outer(result, a, b);
    return result;
  }

  public static Mx myOuter(Vec a, Vec b) {
      final Mx result = new VecBasedMx(a.dim(), b.dim());

      final VecIterator itA = a.nonZeroes();
      while (itA.advance()) {
          final int i = itA.index();
          final VecIterator itB = b.nonZeroes();
          while (itB.advance()) {
              final int j = itB.index();
              result.adjust(i, j, itB.value() * itA.value());
          }
      }
      return result;
  }

  public static Vec fill(Vec x, double val) {
    for (int i = 0; i < x.dim(); i++) {
      x.set(i, val);
    }
    return x;
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
          val -= l.get(i, k) *  l.get(j, k);
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
        inverse.set(i, j, sum*inverse.get(i,i));
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
    final Mx result = new VecBasedMx(dim, new SparseVec<IntBasis>(new IntBasis(dim * dim)));
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
          incscale(resultRow, b.row(t), scale);
      }
    }
    return result;
  }

  public static void incscale(Vec result, Vec left, double scale) {
    if (left instanceof ArrayVec && left.getClass().equals(result.getClass())) {
      final ArrayVec larr = (ArrayVec) left;
      final ArrayVec resarr = (ArrayVec) result;
      ArrayTools.incscale(larr.array, larr.start, resarr.array, resarr.start, larr.length, scale);
    }
    else {
      final VecIterator liter = left.nonZeroes();
      while (liter.advance()) {
        result.adjust(liter.index(), scale * liter.value());
      }
    }
  }

  public static <T extends Vec> T copy(T vec) {
    if (vec instanceof VecBasedMx) {
      final VecBasedMx mx = (VecBasedMx) vec;
      return (T)new VecBasedMx(mx.columns(), copy(mx.vec));
    }
    if (vec instanceof ArrayVec) {
      final ArrayVec arrayVec = (ArrayVec) vec;
      return (T)new ArrayVec(arrayVec.array.clone(), arrayVec.start, arrayVec.length);
    }
    return (T)copySparse(vec);
  }

  public static <T extends  Vec> T sum(final Vec a, final Vec b) {
      Vec result = copy(a);
      return (T)append(result, b);
  }

  public static <T extends Vec> T subtract(final Vec a, final Vec b) {
    Vec result = copy(a);
    for (int i = 0; i < b.dim(); i++)
        result.adjust(i, -1.0 * b.get(i));
      return (T)result;
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
    final int columns = a.columns();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < i; j++) {
        double save = a.get(i, j);
        a.set(i, j, a.get(j, i));
        a.set(j, i, save);
      }
    }
    return a;
  }

  public static void assign(Vec a, Vec vec) {
    if (a instanceof VecBasedMx)
      assign(((VecBasedMx) a).vec, vec);
    if (vec instanceof VecBasedMx)
      assign(a, ((VecBasedMx) vec).vec);
    if (vec instanceof ArrayVec && a instanceof ArrayVec)
      ((ArrayVec)a).assign((ArrayVec)vec);
    else {
      final VecIterator aiter = a.nonZeroes();
      while (aiter.advance())
        aiter.setValue(0.);
      append(a, vec);
    }
  }

  public static Vec multiply(Mx mx, Vec vec) {
    return multiply(mx, new VecBasedMx(1, vec));
  }

  public static Mx mahalanobis(List<Vec> pool) {
    final int dim = pool.get(0).dim();
    Vec mean = new ArrayVec(dim);
    Mx covar = new VecBasedMx(dim, dim);
    for (int i = 0; i < pool.size(); i++) {
      append(mean, pool.get(i));
    }
    scale(mean, -1. / pool.size());
    Vec temp = new ArrayVec(dim);
    for (Vec vec : pool) {
      assign(temp, vec);
      append(temp, mean);
      addOuter(covar, temp, temp);
    }
    scale(covar, 1. / pool.size());
    final Mx l = choleskyDecomposition(covar);
    return inverseLTriangle(l);
  }

  public static void householderLQ(Mx A, Mx L, Mx Q) {
    final int cols = A.columns();
    final int rows = A.rows();
    assign(L, A);
    if (Q != null) {
      scale(Q, 0.);
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
      hhplane.set(i, (origDiag - diag)/r);
      for (int j = i + 1; j < cols; j++) {
        hhplane.set(j, L.get(i, j) / r);
      }
      L.set(i, i, diag);
      for (int j = i + 1; j < cols; j++) {
        L.set(i, j, 0.);
      }
      for (int k = i+1; k < rows; k++) {
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

  public static double sum(Vec target) {
    final VecIterator iterator = target.nonZeroes();
    double sum = 0;
    while(iterator.advance()) {
      sum += iterator.value();
    }
    return sum;
  }

  public static double sum2(Vec target) {
    final VecIterator iterator = target.nonZeroes();
    double sum2 = 0;
    while(iterator.advance()) {
      sum2 += iterator.value() * iterator.value();
    }
    return sum2;
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

    assign(sigma, similar);
    assign(q, joinedInvertedTransform);

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

  public static Vec fillIndices(Vec vec, int[] indices, double x) {
    for (int i = 0; i < indices.length; i++) {
      vec.set(indices[i], x);
    }
    return vec;
  }

  public static Mx inverseCholesky(Mx a) {
    final Mx l = choleskyDecomposition(a);
    final Mx inverseL = inverseLTriangle(l);
    return multiply(transpose(inverseL), inverseL);
  }

  public static void normalizeL1(Vec row) {
    double sum = 0;
    {
      final VecIterator it = row.nonZeroes();
      while(it.advance()) {
        sum += Math.abs(it.value());
      }
    }
    {
      final VecIterator it = row.nonZeroes();

      while(it.advance()) {
        it.setValue(it.value() / sum);
      }
    }
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

  public static double entropy(Vec prob) {
    normalizeL1(prob);
    double entropy = 0;
    for (int i = 0; i < prob.dim(); i++) {
      final double p = prob.get(i);
      entropy += p * log(p);
    }
    return -entropy;
  }

  public static Vec extendVec(Vec sourceVec, double[] addedValues) {
    Vec result = new ArrayVec(sourceVec.dim() + addedValues.length);
    for (VecIterator iter = sourceVec.nonZeroes(); iter.advance(); ) {
      result.set(iter.index(), iter.value());
    }
    for (int i = 0; i < addedValues.length; i++) {
      result.set(i + sourceVec.dim(), addedValues[i]);
    }
    return result;
  }

  private static class IndexedVecIter {
    VecIterator iter;
    int index;

    private IndexedVecIter(VecIterator iter, int index) {
      this.iter = iter;
      this.index = index;
    }
  }
  private static class VecIterEntry extends RBTreeNodeBase {
    List<IndexedVecIter> iters = new LinkedList<IndexedVecIter>();
    int index;

    public VecIterEntry(int index) {
      this.index = index;
    }

    @Override
    public int compareTo(RBTreeNode node) {
      final VecIterEntry entry = (VecIterEntry) node;
      return index - entry.index;
    }
  }

  public static <T extends Vec> double[] multiplyAll(List<T> left, Vec right) {
    double[] result = new double[left.size()];

    final RedBlackTree<VecIterEntry> iters = new RedBlackTree<VecIterEntry>();
    final TIntObjectHashMap<VecIterEntry> cache = new TIntObjectHashMap<VecIterEntry>();
    {
      int index = 0;
      for (Vec vec : left) {
        final VecIterator iter = vec.nonZeroes();
        if (iter.advance())
          processIter(iters, cache, new IndexedVecIter(iter, index++));
      }
    }
    final VecIterator riter = right.nonZeroes();
    VecIterEntry topEntry = iters.pollFirst();

    while (riter.advance() && topEntry != null) {
      final int currentIndex = riter.index();
      final double currentValue = riter.value();

      while (topEntry != null && topEntry.index < currentIndex) {
        for (IndexedVecIter iter : topEntry.iters) {
          while (iter.iter.index() < currentIndex)
            if(!iter.iter.advance()) break;
          if (iter.iter.isValid())
            processIter(iters, cache, iter);
        }
        cache.remove(topEntry.index);
        topEntry = iters.pollFirst();
      }

      if (topEntry != null && topEntry.index == currentIndex) {
        for (IndexedVecIter iter : topEntry.iters) {
          result[iter.index] += currentValue * iter.iter.value();
          if (iter.iter.advance())
            processIter(iters, cache, iter);
        }
        cache.remove(currentIndex);
        topEntry = iters.pollFirst();
      }
    }
    return result;
  }

  private static void processIter(Set<VecIterEntry> iters, TIntObjectHashMap<VecIterEntry> cache, IndexedVecIter iter) {
    final int index = iter.iter.index();
    VecIterEntry iterEntry = cache.get(index);
    if (iterEntry == null) {
      iterEntry = new VecIterEntry(index);
      iters.add(iterEntry);
      cache.put(index, iterEntry);
    }
    iterEntry.iters.add(iter);
  }

  public static double cosine(Vec left, Vec right) {
    double scalarMultiplication = multiply(left, right);
    return scalarMultiplication != 0.0 ? scalarMultiplication / (norm(left) * norm(right)) : 0.0;
  }

  public static double norm(Vec v) {
    return sqrt(sum2(v));
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static double norm1(Vec v) {
    final VecIterator iter = v.nonZeroes();
    double result = 0;
    while (iter.advance()) {
      result += Math.abs(iter.value());
    }

    return result;
  }


  private static int countNonZeroesUpperBound(Vec v) {
    if (v instanceof SparseVec) {
      return ((SparseVec) v).indices.size();
    }
    return v.dim();
  }

  public static <T extends Vec> T scale(T vector, double factor) {
    if (vector instanceof VecBasedMx) {
      scale(((VecBasedMx) vector).vec, factor);
      return vector;
    }
    if (Math.abs(factor) < EPSILON) {
      if (vector instanceof SparseVec) {
        final SparseVec sparseVec = (SparseVec) vector;
        sparseVec.values.resetQuick();
        sparseVec.indices.resetQuick();
        return vector;
      }
      else if (vector instanceof ArrayVec) {
        ((ArrayVec)vector).scale(factor);
      }
    }
    final VecIterator iter = vector.nonZeroes();
    while (iter.advance()) {
      iter.setValue(iter.value() * factor);
    }
    return vector;
  }

  public static <T extends Vec> T toBinary(T vector) {
    final VecIterator iter = vector.nonZeroes();
    while (iter.advance()) {
      iter.setValue(1);
    }
    return vector;
  }

  public static <A> double infinityNorm(final DVector<A> vector) {
    return MathTools.max(vector.values());
  }

  public static <A> double oneNorm(final DVector<A> vector) {
    return MathTools.sum(vector.values());
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
    VecTools.scale(covar, 1./ds.rows());
    switch (type) {
      case SPHERE:
        final Mx l = VecTools.choleskyDecomposition(covar);
        trans = VecTools.inverseLTriangle(l);
        break;
      case PCA:
        trans = new VecBasedMx(ds.columns(), ds.columns());
        VecTools.eigenDecomposition(covar, new VecBasedMx(ds.columns(), ds.columns()), trans);
        break;
      case SCALE:
        trans = new VecBasedMx(ds.columns(), ds.columns());
        for (int i = 0; i < trans.columns(); i++) {
          trans.set(i, i, 1./Math.sqrt(covar.get(i, i)));
        }
        break;
      default:
        throw new NotImplementedException();
    }
    Mx normalized = VecTools.copy(ds);
    for (int i = 0; i < ds.rows(); i++) {
      Vec row = normalized.row(i);
      VecTools.append(row, mean);
      VecTools.assign(row, VecTools.multiply(trans, row));
    }
    props.xMean = mean;
    props.xTrans = trans;
    return normalized;
  }

  public static Vec abs(Vec v) {
    Vec result = copy(v);
    final VecIterator it = result.nonZeroes();
    while (it.advance()) {
      it.setValue(Math.abs(it.value()));
    }
    return result;
  }

  public static Vec join(List<Vec> vectors) {
    int dim = 0;
    for (Vec vec : vectors)
      dim += vec.dim();

    Vec result = new ArrayVec(dim);
    int offset = 0;
    for (Vec vec : vectors) {
      for (int j = 0; j < vec.dim(); j++) {
        result.set(offset + j, vec.get(j));
      }
      offset += vec.dim();
    }
    return result;
  }
}
