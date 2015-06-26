package com.spbsu.commons.math.vectors;

import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.math.vectors.impl.mx.VecBasedMx;
import com.spbsu.commons.math.vectors.impl.vectors.*;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.seq.IntSeq;
import com.spbsu.commons.util.ArrayTools;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.lang.Math.log;
import static java.lang.Math.sqrt;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:28:37
 */
@SuppressWarnings("UnusedDeclaration")
public class VecTools {
  private static final double EPSILON = 1e-6;

  public static int hashCode(final Vec v) {
    int hashCode = v.dim();
    final VecIterator iter = v.nonZeroes();
    while (iter.advance()) {
      hashCode <<= 1;
      hashCode += iter.index();
      hashCode += iter.value() * 10000;
    }
    return hashCode;
  }

  public static boolean equals(final Vec left, final Vec right) {
    return equals(left, right, MathTools.EPSILON);
  }

  public static boolean equals(final Vec left, final Vec right, double epsilon) {
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
      if (Math.abs(left.get(i) - right.get(i)) > epsilon )
        return false;
    }

    return true;
  }

  public static <T extends Vec> T append(final T leftOrig, final Vec... rest) {
    Vec left = leftOrig;
    if (left instanceof VecBasedMx)
      left = ((VecBasedMx)left).vec;

    for (Vec vec : rest) {
      checkBasisesEquals(leftOrig, vec);
      if (vec instanceof VecBasedMx)
        vec = ((VecBasedMx)vec).vec;
      if (left instanceof CustomBasisVec) {
        final VecIterator iterRight = vec.nonZeroes();
        final int maxSize = countNonZeroesUpperBound(left) + countNonZeroesUpperBound(vec);
        final TIntArrayList newIndeces = new TIntArrayList(maxSize);
        final TDoubleArrayList newValues = new TDoubleArrayList(maxSize);
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

  public static double multiply(final Vec left, final Vec right) {
    checkBasisesEquals(left, right);
    if (left instanceof ArrayVec && right instanceof ArrayVec) {
      return ((ArrayVec) left).mul((ArrayVec) right);
    }
    final VecIterator liter = left.nonZeroes();
    final VecIterator riter = right.nonZeroes();
    return multiply(liter, riter);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  private static double multiply(final VecIterator liter, final VecIterator riter) {
    double result = 0;
    if (!liter.advance() || !riter.advance())
      return 0;
    while (liter.isValid() && riter.isValid()) {
      final int lindex = liter.index();
      int rindex = riter.index();
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

  public static double distanceAV(final Vec left, final Vec right) {
    checkBasisesEquals(left, right);
    final int size = left.dim();
    double result = 0;
    for (int i = 0; i < size; i++) {
      final double lv = left.get(i);
      final double rv = right.get(i);
      result += (lv - rv) * (lv - rv);
    }
    return sqrt(result);
  }

  public static double distanceAVJS12(final Vec left, final Vec right) {
    checkBasisesEquals(left, right);
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

  private static void checkBasisesEquals(final Vec left, final Vec right) {
    if (left.dim() != right.dim()) {
      if (left instanceof CustomBasisVec && right instanceof CustomBasisVec) {
        if (!((CustomBasisVec) left).basis().equals(((CustomBasisVec) right).basis()))
          throw new IllegalArgumentException("Vector basises are not the same");
      }
      else throw new IllegalArgumentException("Vector dimensions differs");
    }
  }

  public static double distanceL1(final Vec left, final Vec right) {
    checkBasisesEquals(left, right);

    final VecIterator lIter = left.nonZeroes();
    final VecIterator rIter = right.nonZeroes();
    lIter.advance();
    rIter.advance();

    double result = 0.0;

    while (lIter.isValid() || rIter.isValid()) {
      if (lIter.index() == rIter.index()) {
        result += Math.abs(lIter.value() - rIter.value());
        lIter.advance();
        rIter.advance();
      }
      else if (lIter.index() < rIter.index()) {
        result += Math.abs(lIter.value());
        lIter.advance();
      }
      else if (lIter.index() > rIter.index()) {
        result += Math.abs(rIter.value());
        rIter.advance();
      }
    }

    return result;
  }

  public static double distance(Vec left, Vec right) {
     return Math.sqrt(l2(left,right));
  }

  public static double distanceJS12(final Vec left, final Vec right) {
    checkBasisesEquals(left, right);
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
  public static SparseVec copySparse(final Vec vec) {
    final SparseVec copy;
    if (vec instanceof SparseVec)
      copy = new SparseVec(((SparseVec)vec).basis().size());
    else
      copy = new SparseVec(vec.dim());
    append(copy, vec);
    return copy;
  }

  public static double l1(final Vec vec) {
    double result = 0;
    final VecIterator iterator = vec.nonZeroes();
    while (iterator.advance()) {
      result += Math.abs(iterator.value());
    }
    return result;
  }

  public static void scale(final Vec vec, final Vec scale) {
    final VecIterator iterator = vec.nonZeroes();
    while (iterator.advance()) {
      iterator.setValue(iterator.value() * scale.get(iterator.index()));
    }
  }

  public static boolean checkOrthogonality(final Mx a) {
    if (a.rows() != a.columns())
      return false;
    final Mx square = MxTools.multiply(a, MxTools.transpose(a));
    return equals(square, MxTools.E(a.columns()));
  }

  public static void addOuter(final Mx result, final Vec a, final Vec b) {
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

  public static Mx outer(final Vec a, final Vec b) {
    final VecBasedMx result = new VecBasedMx(a.dim(), b.dim());
    {
      final VecIterator itA = a.nonZeroes();
      while (itA.advance()) {
        final int rowStart = itA.index() * b.dim();
        final VecIterator itB = b.nonZeroes();
        while (itB.advance()) {
          result.set(rowStart + itB.index(), itB.value() * itA.value());
        }
      }
    }
    return result;
  }

  public static <T extends Vec> T fill(final T x, final double val) {
    if (Math.abs(val) < MathTools.EPSILON) {
      if (x instanceof SparseVec) {
        ((SparseVec) x).clear();
        return x;
      }
    }
    if (x instanceof ArrayVec) {
      ((ArrayVec) x).fill(val);
      return x;
    }

    for (int i = 0; i < x.dim(); i++) {
      x.set(i, val);
    }
    return x;
  }

  public static void incscale(final Vec result, final Vec left, final double scale) {
    if (Double.isNaN(scale))
      throw new IllegalArgumentException();
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
  public static <T extends Vec> T copy(final T vec) {
    if (vec instanceof VecBasedMx) {
      final VecBasedMx mx = (VecBasedMx) vec;
      return (T)new VecBasedMx(mx.columns(), copy(mx.vec));
    }
    if (vec instanceof ArrayVec) {
      final ArrayVec arrayVec = (ArrayVec) vec;
      return (T)new ArrayVec(arrayVec.data.array.clone(), arrayVec.data.start, arrayVec.data.length);
    }
    if (vec instanceof IndexTransVec) {
      final IndexTransVec indexTransVec = (IndexTransVec) vec;
      if (VecTools.isSparse(indexTransVec, 0.1))
        return (T) copySparse(indexTransVec);
      else
        return (T) new ArrayVec(indexTransVec.toArray());
    }
    return (T)copySparse(vec);
  }

  @SuppressWarnings("unchecked")
  public static <T extends  Vec> T sum(final Vec a, final Vec b) {
      final Vec result = copy(a);
      return (T)append(result, b);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Vec> T subtract(final Vec a, final Vec b) {
    final Vec result = copy(a);
    for (int i = 0; i < b.dim(); i++)
        result.adjust(i, -1.0 * b.get(i));
      return (T)result;
  }

  public static Vec adjust(final Vec a, double inc) {
    for (int i=0; i < a.dim();++i)
      a.adjust(i,inc);
    return a;
  }

  public static void assign(final Vec target, final Vec source) {
    if (target.length() != source.length()) {
      throw new IllegalArgumentException("Vector dimensions differ");
    }
    if (target instanceof VecBasedMx) {
      assign(((VecBasedMx) target).vec, source);
    }
    else if (source instanceof VecBasedMx) {
      assign(target, ((VecBasedMx) source).vec);
    }
    else if (source instanceof ArrayVec && target instanceof ArrayVec)
      ((ArrayVec)target).assign((ArrayVec)source);
    else {
      final VecIterator aiter = target.nonZeroes();
      while (aiter.advance())
        aiter.setValue(0.);
      append(target, source);
    }
  }

  public static double sum(final Vec target) {
    final VecIterator iterator = target.nonZeroes();
    double sum = 0;
    while(iterator.advance()) {
      sum += iterator.value();
    }
    return sum;
  }

  public static double sum2(final Vec target) {
    final VecIterator iterator = target.nonZeroes();
    double sum2 = 0;
    while(iterator.advance()) {
      sum2 += iterator.value() * iterator.value();
    }
    return sum2;
  }

  public static Vec fillIndices(final Vec vec, final int[] indices, final double x) {
    for (final int index : indices) {
      vec.set(index, x);
    }
    return vec;
  }

  public static void normalizeL1(final Vec row) {
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

  public static double entropy(final Vec prob) {
    normalizeL1(prob);
    double entropy = 0;
    for (int i = 0; i < prob.dim(); i++) {
      final double p = prob.get(i);
      entropy += p * log(p);
    }
    return -entropy;
  }

  public static Vec extendVec(final Vec sourceVec, final double[] addedValues) {
    final Vec result = new ArrayVec(sourceVec.dim() + addedValues.length);
    for (final VecIterator iter = sourceVec.nonZeroes(); iter.advance(); ) {
      result.set(iter.index(), iter.value());
    }
    for (int i = 0; i < addedValues.length; i++) {
      result.set(i + sourceVec.dim(), addedValues[i]);
    }
    return result;
  }

  public static Vec[] splitMxColumns(final Mx features) {
    return null;
  }

  public static Vec concat(final Vec a, final Vec b) {
    final Vec result;
    if (a instanceof SingleValueVec && b instanceof SingleValueVec && a.get(0) == b.get(0)) {
      result = new SingleValueVec(a.get(0), a.dim() + b.dim());
    }
    else if (a instanceof SparseVec || b instanceof SparseVec) {
      result = new SparseVec(a.dim() + b.dim());
      copyTo(a, result, 0);
      copyTo(b, result, a.dim());
    }
    else {
      result = new ArrayVec(a.dim() + b.dim());
      copyTo(a, result, 0);
      copyTo(b, result, a.dim());
    }
    return result;
  }

  public static void copyTo(final Vec a, final Vec to, final int offset) {
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

  public static int l0(final Vec vec) {
    final VecIterator it = vec.nonZeroes();
    int count = 0;
    while (it.advance()) {
      count++;
    }

    return count;
  }

  public static double l2(Vec left, Vec right) {
    checkBasisesEquals(left, right);
    if (left instanceof VecBasedMx) {
      left = ((VecBasedMx)left).vec;
    }
    if (right instanceof VecBasedMx) {
      right = ((VecBasedMx)right).vec;
    }
    if (left instanceof ArrayVec && right instanceof ArrayVec) {
      final ArrayVec larray = (ArrayVec) left;
      final ArrayVec rarray = (ArrayVec) right;
      return larray.l2(rarray);
    }

    final VecIterator lIter = left.nonZeroes();
    final VecIterator rIter = right.nonZeroes();
    lIter.advance();
    rIter.advance();

    double result = 0.0;

    while (lIter.isValid() || rIter.isValid()) {
      if (lIter.index() == rIter.index()) {
        result += (lIter.value() - rIter.value()) * (lIter.value() - rIter.value());
        lIter.advance();
        rIter.advance();
      }
      else if (lIter.index() < rIter.index()) {
        result += lIter.value() * lIter.value();
        lIter.advance();
      }
      else if (lIter.index() > rIter.index()) {
        result += rIter.value() * rIter.value();
        rIter.advance();
      }
    }
    return result;
  }

  public static <T extends Vec> T fillGaussian(T vec, FastRandom rng) {
    for (int i = 0; i < vec.length(); i++) {
      vec.set(i, rng.nextGaussian());
    }
    return vec;
  }

  public static <T extends Vec> T fillUniform(T vec, FastRandom rng) {
    return fillUniform(vec, rng, 1.);
  }

  public static <T extends Vec> T fillUniform(T vec, FastRandom rng, double scale) {
    for (int i = 0; i < vec.length(); i++) {
      vec.set(i, (rng.nextBoolean() ? 1 : -1) * rng.nextDouble() * scale);
    }
    return vec;
  }

  private static class IndexedVecIter {
    VecIterator iter;
    int index;

    private IndexedVecIter(final VecIterator iter, final int index) {
      this.iter = iter;
      this.index = index;
    }
  }
  private static class VecIterEntry implements Comparable<VecIterEntry> {
    List<IndexedVecIter> iters = new LinkedList<>();
    int index;

    public VecIterEntry(final int index) {
      this.index = index;
    }

    @Override
    public int compareTo(@NotNull final VecIterEntry node) {
      return index - node.index;
    }
  }

  public static <T extends Vec> double[] multiplyAll(final List<T> left, final Vec right) {
    final double[] result = new double[left.size()];

    final TreeSet<VecIterEntry> iters = new TreeSet<>();
    final TIntObjectHashMap<VecIterEntry> cache = new TIntObjectHashMap<>();
    {
      int index = 0;
      for (final Vec vec : left) {
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
        for (final IndexedVecIter iter : topEntry.iters) {
          while (iter.iter.index() < currentIndex)
            if(!iter.iter.advance()) break;
          if (iter.iter.isValid())
            processIter(iters, cache, iter);
        }
        cache.remove(topEntry.index);
        topEntry = iters.pollFirst();
      }

      if (topEntry != null && topEntry.index == currentIndex) {
        for (final IndexedVecIter iter : topEntry.iters) {
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

  private static void processIter(final Set<VecIterEntry> iters, final TIntObjectHashMap<VecIterEntry> cache, final IndexedVecIter iter) {
    final int index = iter.iter.index();
    VecIterEntry iterEntry = cache.get(index);
    if (iterEntry == null) {
      iterEntry = new VecIterEntry(index);
      iters.add(iterEntry);
      cache.put(index, iterEntry);
    }
    iterEntry.iters.add(iter);
  }

  public static double cosine(final Vec left, final Vec right) {
    final double scalarMultiplication = multiply(left, right);
    return scalarMultiplication != 0.0 ? scalarMultiplication / (norm(left) * norm(right)) : 0.0;
  }

  public static double infNorm(final Vec v) {
    double result = 0.;
    for (final VecIterator iter = v.nonZeroes(); iter.advance(); ) {
      result = Math.max(result, Math.abs(iter.value()));
    }
    return result;
  }

  public static double norm(final Vec v) {
    return sqrt(sum2(v));
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static double norm1(final Vec v) {
    final VecIterator iter = v.nonZeroes();
    double result = 0;
    while (iter.advance()) {
      result += Math.abs(iter.value());
    }

    return result;
  }


  private static int countNonZeroesUpperBound(final Vec v) {
    if (v instanceof CustomBasisVec) {
      return ((CustomBasisVec) v).indices.size();
    }
    return v.dim();
  }

  public static <T extends Vec> T scale(final T vector, final double factor) {
    if (factor < EPSILON)
      return fill(vector, 0.);
    if (vector instanceof VecBasedMx) {
      scale(((VecBasedMx) vector).vec, factor);
      return vector;
    }
    else if (vector instanceof ArrayVec) {
      ((ArrayVec)vector).scale(factor);
      return vector;
    }
    final VecIterator iter = vector.nonZeroes();
    while (iter.advance()) {
      iter.setValue(iter.value() * factor);
    }
    return vector;
  }

  public static <T extends Vec> T toBinary(final T vector) {
    return toBinary(vector, 0.0);
  }

  public static <T extends Vec> T toBinary(final T vector, final double threshold) {
    final VecIterator iter = vector.nonZeroes();
    while (iter.advance()) {
      if (iter.value() > threshold) {
        iter.setValue(1);
      } else {
        iter.setValue(0);
      }
    }
    return vector;
  }

  public static <A> double infinityNorm(final DVector<A> vector) {
    return MathTools.max(vector.values());
  }

  public static <A> double oneNorm(final DVector<A> vector) {
    return MathTools.sum(vector.values());
  }

  public static Vec abs(final Vec v) {
    final Vec result = copy(v);
    final VecIterator it = result.nonZeroes();
    while (it.advance()) {
      it.setValue(Math.abs(it.value()));
    }
    return result;
  }

  public static Vec join(final List<Vec> vectors) {
    int dim = 0;
    for (final Vec vec : vectors)
      dim += vec.dim();

    final Vec result = new ArrayVec(dim);
    int offset = 0;
    for (final Vec vec : vectors) {
      for (int j = 0; j < vec.dim(); j++) {
        result.set(offset + j, vec.get(j));
      }
      offset += vec.dim();
    }
    return result;
  }

  public static int argmax(final Vec v) {
    int argmax = 0;
    double maxValue = Double.NEGATIVE_INFINITY;
    final VecIterator iter = v.nonZeroes();
    while (iter.advance()) {
      if (iter.value() > maxValue) {
        maxValue = iter.value();
        argmax = iter.index();
      }
    }
    return argmax;
  }

  public static IntSeq toIntSeq(final Vec v) {
    final int[] ints = new int[v.length()];
    final VecIterator iter = v.nonZeroes();
    while (iter.advance()) {
      ints[iter.index()] = (int) iter.value();
    }
    return new IntSeq(ints);
  }

  public static Vec fromIntSeq(final IntSeq seq) {
    final Vec result = new ArrayVec(seq.length());
    for (int i = 0; i < seq.length(); i++) {
      result.set(i, seq.intAt(i));
    }
    return result;
  }

  //it's assuming that idxs is sorted
  public static SparseVec cutSparseVec(final SparseVec sourceVec, final int[] idxs) {
    final TIntList sourceIdxs = sourceVec.indices;
    final TDoubleList sourceValues = sourceVec.values;
    final TIntList newIdxs = new TIntArrayList(Math.min(sourceIdxs.size(), idxs.length));
    final TDoubleList newValues = new TDoubleArrayList(Math.min(sourceIdxs.size(), idxs.length));

    int iPos = 0;
    int jPos = 0;
    while (iPos < sourceIdxs.size() && jPos < idxs.length) {
      if (sourceIdxs.get(iPos) < idxs[jPos]) {
        iPos++;
      }
      else if (sourceIdxs.get(iPos) > idxs[jPos]) {
        jPos++;
      } else {
        newIdxs.add(jPos);
        newValues.add(sourceValues.get(iPos));
        iPos++;
        jPos++;
      }
    }
    return new SparseVec(idxs.length, newIdxs.toArray(), newValues.toArray());
  }
}
