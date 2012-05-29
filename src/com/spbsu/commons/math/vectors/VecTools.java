package com.spbsu.commons.math.vectors;

import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.util.RBTreeNode;
import com.spbsu.commons.util.RBTreeNodeBase;
import com.spbsu.commons.util.RedBlackTree;
import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.util.*;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:28:37
 */
public class VecTools {
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

  public static <T extends Vec> T append(final T left, final Vec... rest) {
    for (Vec vec : rest) {
      if (!left.basis().equals(vec.basis()))
        throw new IllegalArgumentException("vectors basises differ");
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

    return left;
  }

  public static double multiply(Vec left, Vec right) {
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
    if (!left.basis().equals(right.basis()))
      throw new IllegalArgumentException("Vector basises are not the same");
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
