package com.spbsu.commons.math.vectors;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.math.vectors.impl.mx.VecBasedMx;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.math.vectors.impl.vectors.CustomBasisVec;
import com.spbsu.commons.math.vectors.impl.vectors.DVector;
import com.spbsu.commons.math.vectors.impl.vectors.SparseVec;
import com.spbsu.commons.util.ArrayTools;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

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
    if (left instanceof Mx && right instanceof Mx)
      if (((Mx)left).columns() != ((Mx) right).columns())
        return false;
    if (left.getClass() == right.getClass()) {
      if (left instanceof CustomBasisVec) {
        return ((CustomBasisVec) left).indices.equals(((CustomBasisVec) right).indices)
                && ((CustomBasisVec) left).values.equals(((CustomBasisVec) right).values);
      } else if (left instanceof ArrayVec) {
        final ArrayVec larray = (ArrayVec) left;
        final ArrayVec rarray = (ArrayVec) right;

        if (larray.data.array == rarray.data.array && larray.data.start == rarray.data.start && larray.data.length == rarray.data.length)
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
      if (left instanceof CustomBasisVec) {
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
        ((CustomBasisVec) left).indices = newIndeces;
        ((CustomBasisVec) left).values = newValues;
      }
      else if (left instanceof ArrayVec && left.getClass().equals(vec.getClass())) {
        ((ArrayVec)left).add((ArrayVec)vec);
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

  @SuppressWarnings("StatementWithEmptyBody")
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

  @SuppressWarnings("unchecked")
  public static SparseVec copySparse(Vec vec) {
    SparseVec copy;
    if (vec instanceof SparseVec)
      copy = new SparseVec(((SparseVec)vec).basis().size());
    else
      copy = new SparseVec(vec.dim());
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

  public static boolean checkOrthogonality(Mx a) {
    if (a.rows() != a.columns())
      return false;
    final Mx square = MxTools.multiply(a, MxTools.transpose(a));
    return equals(square, MxTools.E(a.columns()));
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
    return result;
  }

  public static Vec fill(Vec x, double val) {
    for (int i = 0; i < x.dim(); i++) {
      x.set(i, val);
    }
    return x;
  }

  public static void incscale(Vec result, Vec left, double scale) {
    if (left instanceof ArrayVec && left.getClass().equals(result.getClass())) {
      final ArrayVec larr = (ArrayVec) left;
      final ArrayVec resarr = (ArrayVec) result;
      ArrayTools.incscale(larr.data.array, larr.data.start, resarr.data.array, resarr.data.start, larr.data.length, scale);
    }
    else {
      final VecIterator liter = left.nonZeroes();
      while (liter.advance()) {
        result.adjust(liter.index(), scale * liter.value());
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends Vec> T copy(T vec) {
    if (vec instanceof VecBasedMx) {
      final VecBasedMx mx = (VecBasedMx) vec;
      return (T)new VecBasedMx(mx.columns(), copy(mx.vec));
    }
    if (vec instanceof ArrayVec) {
      final ArrayVec arrayVec = (ArrayVec) vec;
      return (T)new ArrayVec(arrayVec.data.array.clone(), arrayVec.data.start, arrayVec.data.length);
    }
    return (T)copySparse(vec);
  }

  @SuppressWarnings("unchecked")
  public static <T extends  Vec> T sum(final Vec a, final Vec b) {
      Vec result = copy(a);
      return (T)append(result, b);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Vec> T subtract(final Vec a, final Vec b) {
    Vec result = copy(a);
    for (int i = 0; i < b.dim(); i++)
        result.adjust(i, -1.0 * b.get(i));
      return (T)result;
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

  public static Vec fillIndices(Vec vec, int[] indices, double x) {
    for (final int index : indices) {
      vec.set(index, x);
    }
    return vec;
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

  public static Vec[] splitMxColumns(Mx features) {
    return null;
  }

  public static Vec concat(final Vec a, final Vec b) {
    Vec result;
    if (a instanceof SingleValueVec && b instanceof SingleValueVec && a.get(0) == b.get(0)) {
      result = new SingleValueVec(a.get(0), a.dim() + b.dim());
    }
    else if (a instanceof SparseVec || b instanceof SparseVec) {
      result = new ArrayVec(a.dim() + b.dim());
      copyTo(a, result, 0);
      copyTo(b, result, a.dim());
    }
    else {
      result = new SparseVec(a.dim() + b.dim());
      copyTo(a, result, 0);
      copyTo(b, result, a.dim());
    }
    return result;
  }

  public static void copyTo(final Vec a, final Vec to, int offset) {
    final VecIterator nzI = a.nonZeroes();
    while(nzI.advance())
      to.set(offset + nzI.index(), nzI.value());
  }

  public static boolean isSparse(final Vec vec, final double th) {
    final VecIterator itNz = vec.nonZeroes();
    int nzCount = 0;
    while (itNz.advance()) {
      nzCount++;
    }

    return nzCount/(double)vec.dim() < th;
  }

  private static class IndexedVecIter {
    VecIterator iter;
    int index;

    private IndexedVecIter(VecIterator iter, int index) {
      this.iter = iter;
      this.index = index;
    }
  }
  private static class VecIterEntry implements Comparable<VecIterEntry> {
    List<IndexedVecIter> iters = new LinkedList<>();
    int index;

    public VecIterEntry(int index) {
      this.index = index;
    }

    @Override
    public int compareTo(@NotNull VecIterEntry node) {
      return index - node.index;
    }
  }

  public static <T extends Vec> double[] multiplyAll(List<T> left, Vec right) {
    double[] result = new double[left.size()];

    final TreeSet<VecIterEntry> iters = new TreeSet<>();
    final TIntObjectHashMap<VecIterEntry> cache = new TIntObjectHashMap<>();
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

  public static double infNorm(Vec v) {
    double result = 0.;
    for (VecIterator iter = v.nonZeroes(); iter.advance(); ) {
      result = Math.max(result, Math.abs(iter.value()));
    }
    return result;
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
    if (v instanceof CustomBasisVec) {
      return ((CustomBasisVec) v).indices.size();
    }
    return v.dim();
  }

  public static <T extends Vec> T scale(T vector, double factor) {
    if (vector instanceof VecBasedMx) {
      scale(((VecBasedMx) vector).vec, factor);
      return vector;
    }
    if (Math.abs(factor) < EPSILON) {
      if (vector instanceof CustomBasisVec) {
        final CustomBasisVec sparseVec = (CustomBasisVec) vector;
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
