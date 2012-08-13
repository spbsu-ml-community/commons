package com.spbsu.commons.math.vectors;

import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.util.RBTreeNode;
import com.spbsu.commons.util.RBTreeNodeBase;
import com.spbsu.commons.util.RedBlackTree;
import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:28:37
 */
public class VecTools {
  private static final double EPSILON = 1e-7;

  public static boolean equals(final Vec left, final Vec right) {
    if (!left.basis().equals(right.basis()))
      return false;
    if (left.getClass() == right.getClass()) {
      if (left instanceof SparseVec) {
        return ((SparseVec) left).indexTransform.equals(((SparseVec) right).indexTransform)
            && ((SparseVec) left).values.equals(((SparseVec) right).values);
      } else if (left instanceof ArrayVec) {
        return Arrays.equals(((ArrayVec) left).values, ((ArrayVec) right).values);
      }
    }

    for (int i = 0; i < left.basis().size(); i++) {
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

      if (left instanceof ArrayVec && vec instanceof ArrayVec) {
        final ArrayVec larray = (ArrayVec) left;
        final ArrayVec rarray = (ArrayVec) vec;
        final int dim = larray.dim();
        final int alignedDim = (dim / 4) * 4;
        for (int i = 0; i < alignedDim; i+=4) {
          larray.values[i] += rarray.values[i];
          larray.values[i + 1] += rarray.values[i + 1];
          larray.values[i + 2] += rarray.values[i + 2];
          larray.values[i + 3] += rarray.values[i + 3];
        }

        for (int i = alignedDim; i < dim; i++){
          larray.values[i] += rarray.values[i];
        }
        continue;
      }
      final VecIterator iterRight = vec.nonZeroes();

      if (left instanceof SparseVec) {
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
        ((SparseVec) left).indexTransform = newIndeces;
        ((SparseVec) left).values = newValues;
      }
      else {
        while (iterRight.advance()) {
          left.adjust(iterRight.index(), iterRight.value());
        }
      }
    }

    return leftOrig;
  }

  public static double multiply(Vec left, Vec right) {
    if (left.dim() != right.dim())
      throw new IllegalArgumentException("Vector basises are not of the same size left:" + left.dim() + ", right: " + right.dim());
    if (left instanceof ArrayVec && right instanceof ArrayVec) {
      final ArrayVec larray = (ArrayVec) left;
      final ArrayVec rarray = (ArrayVec) right;
      final int dim = larray.dim();
      double result = 0;
      final int alignedDim = (dim / 4) * 4;
      for (int i = 0; i < alignedDim; i+=4) {
        double l1 = larray.values[i], l2 = larray.values[i + 1], l3 = larray.values[i + 2], l4 = larray.values[i + 3];
        double r1 = rarray.values[i], r2 = rarray.values[i + 1], r3 = rarray.values[i + 2], r4 = rarray.values[i + 3];
        result += l1 * r1 + l2 * r2 + l3 * r3 + l4 * r4;
      }

      for (int i = alignedDim; i < dim; i++){
        double l1 = larray.values[i];
        double r1 = rarray.values[i];
        result += l1 * r1;
      }
      return result;
    }
    final VecIterator liter = left.nonZeroes();
    final VecIterator riter = right.nonZeroes();
    double result = 0;
    if (!liter.advance() || !riter.advance())
      return 0;
    int lindex = liter.index(), rindex = riter.index();
     while (liter.isValid() && riter.isValid()) {
      if (rindex == lindex) {
        result += liter.value() * riter.value();
        if (liter.advance())
          lindex = liter.index();
        if (riter.advance())
          rindex = riter.index();
      }
      else if (lindex > rindex) {
        if(riter.advance())
          rindex = riter.index();
      }
      else {
        if(liter.advance())
          lindex = liter.index();
      }
    }
    return result;
  }

  public static double distanceAV(Vec left, Vec right) {
    if (!left.basis().equals(right.basis()))
      throw new IllegalArgumentException("Vector basises are not the same");
    final int size = left.basis().size();
    double result = 0;
    for (int i = 0; i < size; i++) {
      final double lv = left.get(i);
      final double rv = right.get(i);
      result += (lv - rv) * (lv - rv);
    }
    return Math.sqrt(result);
  }

  public static double distanceAVJS12(Vec left, Vec right) {
    if (!left.basis().equals(right.basis()))
      throw new IllegalArgumentException("Vector basises are not the same");
    final int size = left.basis().size();
    double result = 0;
    for (int i = 0; i < size; i++) {
      final double p = left.get(i);
      final double q = right.get(i);
      final double pr = p == 0 ? 0 : p * Math.log(2 * p / (p + q));
      final double qr = q == 0 ? 0 : q * Math.log(2 * q / (p + q));
      result += pr + qr;
    }
    return Math.sqrt(result);
  }

  public static double distance(Vec left, Vec right) {
    if (left instanceof VecBasedMx)
      left = ((VecBasedMx)left).vec;
    if (right instanceof VecBasedMx)
      right = ((VecBasedMx)right).vec;

    if (!left.basis().equals(right.basis()))
      throw new IllegalArgumentException("Vector basises are not the same");
    if (left instanceof ArrayVec && right instanceof ArrayVec) {
      final ArrayVec larray = (ArrayVec) left;
      final ArrayVec rarray = (ArrayVec) right;
      final int dim = larray.dim();
      double result = 0;
      final int alignedDim = (dim / 4) * 4;
      for (int i = 0; i < alignedDim; i+=4) {
        double r1 = larray.values[i] - rarray.values[i], r2 = larray.values[i + 1] - rarray.values[i + 1],
               r3 = larray.values[i + 2] - rarray.values[i + 2], r4 = larray.values[i + 3] - rarray.values[i + 3];
        result += r1 * r1 + r2 * r2 + r3 * r3 + r4 * r4;
      }

      for (int i = alignedDim; i < dim; i++){
        double l1 = larray.values[i] - rarray.values[i];
        result += l1 * l1;
      }
      return result;
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
    return Math.sqrt(result);
  }

  public static double distanceJS12(Vec left, Vec right) {
    if (!left.basis().equals(right.basis()))
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
        result += p * Math.log(2 * p / (p + q)) + q * Math.log(2 * q / (p + q));
        if (liter.advance())
          lindex = liter.index();
        if (riter.advance())
          rindex = riter.index();
      }
      else if (lindex > rindex) {
        result += riter.value() * Math.log(2);
        if(riter.advance())
          rindex = riter.index();
      }
      else {
        result += liter.value() * Math.log(2);
        if(liter.advance())
          lindex = liter.index();
      }
    }
    return Math.sqrt(result);
  }

  public static Vec copySparse(Vec vec) {
    final SparseVec copy = new SparseVec(vec.basis());
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
        final VecIterator itB = a.nonZeroes();
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

  public static void fill(Vec x, double val) {
    for (int i = 0; i < x.dim(); i++) {
      x.set(i, val);
    }
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
      l.set(i, i, Math.sqrt(diagonal));
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

  public static Mx multiply(Mx a, Mx b) {
    final int dim = a.columns();
    if (dim != b.rows())
      throw new IllegalArgumentException("Matrices must have a.columns == b.rows!");
    final int rows = a.rows();
    final int columns = b.columns();
    final VecBasedMx mx = new VecBasedMx(rows, columns);
    double[] result = ((ArrayVec)mx.vec).values;
    for (int i = 0; i < rows; i++) {
      int offset = i * columns;
      for (int t = 0; t < dim; t++) {
        for (int j = 0; j < columns; j++) {
          result[offset + j] += a.get(i, t) * b.get(t, j);
        }
      }
    }
    return mx;
  }

  public static Vec copy(Vec vec) {
    if (vec instanceof ArrayVec) {
      final ArrayVec arrayVec = (ArrayVec) vec;
      return new ArrayVec(arrayVec.values.clone());
    }
    return copySparse(vec);
  }

  public static Mx transpose(Mx a) {
    final Mx result = new VecBasedMx(a.columns(), a.rows());
    for (int i = 0; i < a.rows(); i++) {
      for (int j = 0; j < a.columns(); j++)
        result.set(j, i, a.get(i, j));
    }
    return result;
  }

  public static void assign(Vec a, Vec vec) {
    scale(a, 0);
    append(a, vec);
  }

  public static Vec multiply(Mx mx, Vec vec) {
    return multiply(mx, new VecBasedMx(1, vec));
  }

  public static Mx mahalanobis(List<Vec> pool) {
    final int dim = pool.get(0).dim();
    Vec mean = new ArrayVec(dim);
    Mx covar = new VecBasedMx(dim, dim);
    for (int i = 0; i < pool.size(); i++) {
      VecTools.append(mean, pool.get(i));
    }
    VecTools.scale(mean, -1./pool.size());
    Vec temp = new ArrayVec(dim);
    for (Vec vec : pool) {
      VecTools.assign(temp, vec);
      VecTools.append(temp, mean);
      VecTools.addOuter(covar, temp, temp);
    }
    VecTools.scale(covar, 1./pool.size());
    final Mx l = VecTools.choleskyDecomposition(covar);
    return VecTools.inverseLTriangle(l);
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

  //  public static <T extends Vec> double[] multiplyAll(List<T> left, Vec right) {
//    double[] result = new double[left.size()];
//    VecIterator[] iters = new VecIterator[left.size()];
//    int index = 0;
//    int nonFinishedCount = 0;
//    for (T v : left) {
//      final VecIterator iter = v.nonZeroes();
//      iters[index++] = iter;
//      if (iter.advance())
//        nonFinishedCount++;
//    }
//
//    final VecIterator riter = right.nonZeroes();
//
//    while (riter.advance() && nonFinishedCount > 0) {
//      final int currentIndex = riter.index();
//      final double currentValue = riter.value();
//      for (int i = 0; i < iters.length; i++) {
//        final VecIterator iter = iters[i];
//        if (iter == null || iter.index() > currentIndex)
//          continue;
//        int current;
//        while ((current = iter.index()) < currentIndex && iter.advance());
//        if (iter.isValid()) {
//          if (current == currentIndex) {
//            result[i] += currentValue * iter.value();
//            if (!iter.advance()) {
//              iters[i] = null;
//              nonFinishedCount--;
//            }
//          }
//        }
//        else {
//          nonFinishedCount--;
//          iters[i] = null;
//        }
//      }
//    }
//    return result;
//  }

  public static double cosine(Vec left, Vec right) {
    double scalarMultiplication = multiply(left, right);
    return scalarMultiplication != 0.0 ? scalarMultiplication / (norm(left) * norm(right)) : 0.0;
  }

  public static double norm(Vec v) {
    final VecIterator iter = v.nonZeroes();
    double result = 0;
    while (iter.advance()) {
      final double value = iter.value();
      result += value * value;
    }

    return Math.sqrt(result);
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
      return ((SparseVec) v).indexTransform.size();
    }
    return v.basis().size();
  }

  public static <T extends Vec> T scale(T vector, double factor) {
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
}
