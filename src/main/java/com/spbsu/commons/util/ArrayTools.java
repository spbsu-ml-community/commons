package com.spbsu.commons.util;

import com.spbsu.commons.filters.Filter;
import com.spbsu.commons.func.Computable;
import com.spbsu.commons.func.Evaluator;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecTools;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;
import com.spbsu.commons.math.vectors.impl.vectors.SparseVec;
import com.spbsu.commons.math.vectors.impl.vectors.VecBuilder;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.seq.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author lawless
 */
public abstract class ArrayTools {
  public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
  public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  public static final int[] EMPTY_INT_ARRAY = new int[0];
  public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
  public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];
  public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];

  private static final FastRandom rng = new FastRandom();

  public static double[] convert(final Double[] array) {
    final int length = array.length;
    if (length == 0) return EMPTY_DOUBLE_ARRAY;
    final double[] result = new double[length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static Double[] convert(final double[] array) {
    final int length = array.length;
    if (length == 0) return EMPTY_DOUBLE_OBJECT_ARRAY;
    final Double[] result = new Double[length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static int[] convert(final Integer[] array) {
    final int length = array.length;
    if (length == 0) return EMPTY_INT_ARRAY;
    final int[] result = new int[length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static Integer[] convert(final int[] array) {
    final int length = array.length;
    if (length == 0) return EMPTY_INTEGER_OBJECT_ARRAY;
    final Integer[] result = new Integer[length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static void parallelSort(final int[] a, final int[] linked) {
    if(a.length != linked.length)
      throw new IllegalArgumentException("arrays sizes are not equal");
//      shuffle(a, linked); // to guard against worst-case
    parallelSort(a, linked, 0, a.length);
  }

  public static void parallelSort(final int[] a, final double[] linked) {
    if(a.length != linked.length)
      throw new IllegalArgumentException("arrays sizes are not equal");
//      shuffle(a, linked); // to guard against worst-case
    parallelSort(a, linked, 0, a.length );
  }

  public static void parallelSort(final double[] a, final int[] linked) {
    if(a.length != linked.length)
      throw new IllegalArgumentException("arrays sizes are not equal");
    parallelSort(a, linked, 0, a.length);
  }

  public static void parallelSort(final long[] a, final int[] linked) {
    if(a.length != linked.length)
      throw new IllegalArgumentException("arrays sizes are not equal");
//      shuffle(a, linked); // to guard against worst-case
    parallelSort(a, linked, 0, a.length );
  }

  public static void parallelSort(final double[] a, final double[] linked1, final double[] linked2) {
    parallelSort(a,linked1,linked2,0,a.length);
  }

  public static void parallelSort(final int[] a, final int[] linked, int left, int right) {
    if (right <= left) return;
    while (left < right) {
      final int i = partition(a, linked, left, right);
      if (right - i > i - left) {
        parallelSort(a, linked, left, i );
        left = i + 1;
      } else {
        parallelSort(a, linked, i + 1, right);
        right = i;
      }
    }
  }

  public static void parallelSort(final int[] a, final double[] linked, int left, int right) {
    if (right <= left) return;
    while (left < right) {
      final int i = partition(a, linked, left, right);
      if (right - i > i - left) {
        parallelSort(a, linked, left, i );
        left = i + 1;
      } else {
        parallelSort(a, linked, i + 1, right);
        right = i;
      }
    }
  }

  public static void parallelSort(final float[] a, final int[] linked, int left, int right) {
    if (right <= left) return;
    while (left < right) {
      final int i = partition(a, linked, left, right);
      if (right - i > i - left) {
        parallelSort(a, linked, left, i);
        left = i + 1;
      } else {
        parallelSort(a, linked, i + 1, right);
        right = i;
      }
    }
  }




  public static void parallelSort(final long[] a, final int[] linked, int left, int right) {
    if (right <= left) return;
    while (left < right) {
      final int i = partition(a, linked, left, right);
      if (right - i > i - left) {
        parallelSort(a, linked, left, i );
        left = i + 1;
      } else {
        parallelSort(a, linked, i + 1, right);
        right = i;
      }
    }
  }

  public static void parallelSort(final double[] a, final int[] linked, int left, int right) {
    if (right <= left) return;
    while (left < right) {
      final int i = partition(a, linked, left, right);
      if (right - i > i - left) {
        parallelSort(a, linked, left, i);
        left = i +1;
      } else {
        parallelSort(a, linked, i + 1, right);
        right = i;
      }
    }
  }

  public static void parallelSort(final double[] a, final double[] linked1, final double[] linked2, int left, int right) {
    if (right <= left) return;
    while (left < right) {
      final int i = partition(a, linked1,linked2, left, right);
      if (right - i > i - left) {
        parallelSort(a, linked1,linked2, left, i);
        left = i + 1;
      } else {
        parallelSort(a, linked1,linked2, i + 1, right);
        right = i;
      }
    }
  }



  private static int partition(final int[] a, final int[] linked, final int left, final int right) {
    int i = left - 1;
    int j = right;
    final int partition = left + rng.nextInt(right - left);
    final double sentinel = a[partition];
    while (true) {
      while (++i < right && a[i] < sentinel);      // find item on left to swap
      while (--j >= left && sentinel < a[j]);      // find item on right to swap
      if (i >= j)
        break;              // check if pointers cross
      else if (i == partition)
        j++;
      else if (j == partition)
        i--;
      else swap(a, linked, i, j);     // swap two elements into place
    }
    if (partition > i)
      swap(a, linked, i, partition);    // swap with partition element
    else if (partition < i)
      swap(a, linked, --i, partition);    // swap with partition element
    return i;
  }

  private static int partition(final int[] a, final double[] linked, final int left, final int right) {
    int i = left - 1;
    int j = right;
    final int partition = left + rng.nextInt(right - left);
    final double sentinel = a[partition];
    while (true) {
      while (++i < right && a[i] < sentinel);      // find item on left to swap
      while (--j >= left && sentinel < a[j]);      // find item on right to swap
      if (i >= j)
        break;              // check if pointers cross
      else if (i == partition)
        j++;
      else if (j == partition)
        i--;
      else swap(a, linked, i, j);     // swap two elements into place
    }
    if (partition > i)
      swap(a, linked, i, partition);    // swap with partition element
    else if (partition < i)
      swap(a, linked, --i, partition);    // swap with partition element
    return i;
  }

  private static int partition(final float[] a, final int[] linked, final int left, final int right) {
    int i = left - 1;
    int j = right;
    final int partition = left + rng.nextInt(right - left);
    final double sentinel = a[partition];
    while (true) {
      while (++i < right && a[i] < sentinel);      // find item on left to swap
      while (--j >= left && sentinel < a[j]);      // find item on right to swap
      if (i >= j)
        break;              // check if pointers cross
      else if (i == partition)
        j++;
      else if (j == partition)
        i--;
      else swap(a, linked, i, j);     // swap two elements into place
    }
    if (partition > i)
      swap(a, linked, i, partition);    // swap with partition element
    else if (partition < i)
      swap(a, linked, --i, partition);    // swap with partition element
    return i;
  }


  private static int partition(final double[] a, final int[] linked, final int left, final int right) {
    int i = left - 1;
    int j = right;
    final int partition = left + rng.nextInt(right - left);
    final double sentinel = a[partition];
    while (true) {
      while (++i < right && a[i] < sentinel);      // find item on left to swap
      while (--j >= left && sentinel < a[j]);      // find item on right to swap
      if (i >= j)
        break;              // check if pointers cross
      else if (i == partition)
        j++;
      else if (j == partition)
        i--;
      else swap(a, linked, i, j);     // swap two elements into place
    }
    if (partition > i)
      swap(a, linked, i, partition);    // swap with partition element
    else if (partition < i)
      swap(a, linked, --i, partition);    // swap with partition element
    return i;
  }


  private static int partition(final long[] a, final int[] linked, final int left, final int right) {
    int i = left - 1;
    int j = right;
    final int partition = left + rng.nextInt(right - left);
    final double sentinel = a[partition];
    while (true) {
      while (++i < right && a[i] < sentinel);      // find item on left to swap
      while (--j >= left && sentinel < a[j]);      // find item on right to swap
      if (i >= j)
        break;              // check if pointers cross
      else if (i == partition)
        j++;
      else if (j == partition)
        i--;
      else swap(a, linked, i, j);     // swap two elements into place
    }
    if (partition > i)
      swap(a, linked, i, partition);    // swap with partition element
    else if (partition < i)
      swap(a, linked, --i, partition);    // swap with partition element
    return i;
  }



  private static int partition(final double[] a, final double[] linked1,  final double[] linked2, final int left, final int right) {
    int i = left - 1;
    int j = right;
    final int partition = left + rng.nextInt(right - left);
    final double sentinel = a[partition];
    while (true) {
      while (++i < right && a[i] < sentinel);      // find item on left to swap
      while (--j >= left && sentinel < a[j]);      // find item on right to swap
      if (i >= j)
        break;              // check if pointers cross
      else if (i == partition)
        j++;
      else if (j == partition)
        i--;
      else {
        swap(a, linked1,linked2, i, j);     // swap two elements into place
      }
    }
    if (partition > i)
      swap(a, linked1,linked2, i, partition);    // swap with partition element
    else if (partition < i)
      swap(a, linked1,linked2, --i, partition);    // swap with partition element
    return i;
  }




  // exchange a[i] and a[j]
  private static void swap(final int[] a, final int[] linked, final int i, final int j) {
    {
      final int swap = a[i];
      a[i] = a[j];
      a[j] = swap;
    }
    {
      final int swap = linked[i];
      linked[i] = linked[j];
      linked[j] = swap;
    }
  }

  // exchange a[i] and a[j]
  private static void swap(final double[] a, final int[] linked, final int i, final int j) {
    {
      final double swap = a[i];
      a[i] = a[j];
      a[j] = swap;
    }
    {
      final int swap = linked[i];
      linked[i] = linked[j];
      linked[j] = swap;
    }
  }


  private static void swap(final double[] a, final double[] linked1,final double[] linked2, final int i, final int j) {
    {
      final double swap = a[i];
      a[i] = a[j];
      a[j] = swap;
    }
    {
      final double swap = linked1[i];
      linked1[i] = linked1[j];
      linked1[j] = swap;
    }

    {
      final double swap = linked2[i];
      linked2[i] = linked2[j];
      linked2[j] = swap;
    }
  }

  private static void swap(final float[] a, final int[] linked, final int i, final int j) {
    {
      final float swap = a[i];
      a[i] = a[j];
      a[j] = swap;
    }
    {
      final int swap = linked[i];
      linked[i] = linked[j];
      linked[j] = swap;
    }
  }

  // exchange a[i] and a[j]
  private static void swap(final int[] a, final double[] linked, final int i, final int j) {
    {
      final int swap = a[i];
      a[i] = a[j];
      a[j] = swap;
    }
    {
      final double swap = linked[i];
      linked[i] = linked[j];
      linked[j] = swap;
    }
  }

  // exchange a[i] and a[j]
  private static void swap(final long[] a, final int[] linked, final int i, final int j) {
    {
      final long swap = a[i];
      a[i] = a[j];
      a[j] = swap;
    }
    {
      final int swap = linked[i];
      linked[i] = linked[j];
      linked[j] = swap;
    }
  }

  // shuffle the array a[]
  private static void shuffle(final int[] a, final double[] linked) {
    final int N = a.length;
    for (int i = 0; i < N; i++) {
      final int r = i + (int) (Math.random() * (N-i));   // between i and N-1
      swap(a, linked, i, r);
    }
  }

  // shuffle the array a[]
  private static void shuffle(final long[] a, final int[] linked) {
    final int N = a.length;
    for (int i = 0; i < N; i++) {
      final int r = i + (int) (Math.random() * (N-i));   // between i and N-1
      swap(a, linked, i, r);
    }
  }

  public static int[] sequence(final int start, final int end) {
    final int[] result = new int[end - start];
    for (int i = 0; i < result.length; i++) {
      result[i] = i + start;
    }
    return result;
  }


  public static int max(final double[] arr) {
    int maxIndex = -1;
    double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < arr.length; i++) {
      if (max < arr[i]) {
        maxIndex = i;
        max = arr[i];
      }

    }
    return maxIndex;
  }

  public static int min(final double[] arr) {
    int minIndex = -1;
    double min = Double.POSITIVE_INFINITY;
    for (int i = 0; i < arr.length; i++) {
      if (min > arr[i]) {
        minIndex = i;
        min = arr[i];
      }

    }
    return minIndex;
  }

  public static <T> int max(final T[] arr, final Evaluator<T> evaluator) {
    int maxIndex = -1;
    double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < arr.length; i++) {
      final double score = evaluator.value(arr[i]);
      if (max < score) {
        maxIndex = i;
        max = score;
      }

    }
    return maxIndex;
  }

  public static <F, T> T[] map(final F[] models, final Class<T> clazz, final Computable<F, T> computable) {
    final T[] result = (T[]) Array.newInstance(clazz, models.length);
    for (int i = 0; i < models.length; i++)
      result[i] = computable.compute(models[i]);
    return result;
  }

  public static <T> double[] score(final T[] dirs, final Evaluator<T> evaluator) {
    final double[] result = new double[dirs.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = evaluator.value(dirs[i]);
    }
    return result;
  }

  public static void add(final double[] larray, final int loffset, final double[] rarray, final int roffset, final int count) {
    final int alignedCount = (count / 4) * 4;
    for (int i = 0; i < alignedCount; i+=4) {
      larray[i + loffset] += rarray[i + roffset];
      larray[i + loffset + 1] += rarray[i + roffset + 1];
      larray[i + loffset + 2] += rarray[i + roffset + 2];
      larray[i + loffset + 3] += rarray[i + roffset + 3];
    }

    for (int i = alignedCount; i < count; i++){
      larray[i + loffset] += rarray[i + roffset];
    }
  }

  public static void fill(final double[] array, final int offset, final int length, final double val) {
    final int alignedCount = (length / 4) * 4 + offset;
    for (int i = offset; i < alignedCount; i+=4) {
      array[i] = val;
      array[i + 1] = val;
      array[i + 2] = val;
      array[i + 3] = val;
    }

    for (int i = alignedCount; i < length + offset; i++){
      array[i] = val;
    }
  }

  public static void mul(final double[] array, final int offset, final int length, final double val) {
    final int alignedCount = (length / 4) * 4 + offset;
    for (int i = offset; i < alignedCount; i+=4) {
      array[i] *= val;
      array[i + 1] *= val;
      array[i + 2] *= val;
      array[i + 3] *= val;
    }

    for (int i = alignedCount; i < length + offset; i++){
      array[i] *= val;
    }
  }

  public static void scale(final double[] larray, final int loffset, final double[] rarray, final int roffset, final int count) {
    final int alignedCount = (count / 4) * 4;
    for (int i = 0; i < alignedCount; i+=4) {
      larray[i + loffset] *= rarray[i + roffset];
      larray[i + loffset + 1] *= rarray[i + roffset + 1];
      larray[i + loffset + 2] *= rarray[i + roffset + 2];
      larray[i + loffset + 3] *= rarray[i + roffset + 3];
    }

    for (int i = alignedCount; i < count; i++){
      larray[i + loffset] *= rarray[i + roffset];
    }
  }

  public static double mul(final double[] larray, int loffset, final double[] rarray, int roffset, final int count) {
    final int alignedEndL = (count / 4) * 4 + loffset;
    final int endL = count + loffset;
    double result = 0;
    for (; loffset < alignedEndL; roffset+=4, loffset+=4) {
      final double r1 = larray[loffset] * rarray[roffset];
      final double r2 = larray[loffset + 1] * rarray[roffset + 1];
      final double r3 = larray[loffset + 2] * rarray[roffset + 2];
      final double r4 = larray[loffset + 3] * rarray[roffset + 3];
      result += r1 + r2 + r3 + r4;
    }

    for (; loffset < endL; roffset++, loffset++){
      result += larray[loffset] * rarray[roffset];
    }
    return result;
  }

  public static void incscale(
      final double[] larray, int loffset, final double[] result, int resultOffset, final int count, final double scale) {
    final int alignedEndL = (count / 4) * 4 + loffset;
    final int endL = count + loffset;
    for (; loffset < alignedEndL; loffset+=4, resultOffset+=4) {
      result[resultOffset] += larray[loffset] * scale;
      result[resultOffset + 1] += larray[loffset + 1] * scale;
      result[resultOffset + 2] += larray[loffset + 2] * scale;
      result[resultOffset + 3] += larray[loffset + 3] * scale;
    }

    for (; loffset < endL; loffset++, resultOffset++){
      result[resultOffset] += larray[loffset] * scale;
    }
  }

  public static double l2(final double[] larray, int loffset, final double[] rarray, int roffset, final int count) {
    final int alignedEndL = (count / 4) * 4 + loffset;
    final int endL = count + loffset;
    double result = 0;
    for (; loffset < alignedEndL; roffset+=4, loffset+=4) {
      final double r1 = larray[loffset] - rarray[roffset];
      final double r2 = larray[loffset + 1] - rarray[roffset + 1];
      final double r3 = larray[loffset + 2] - rarray[roffset + 2];
      final double r4 = larray[loffset + 3] - rarray[roffset + 3];
      result += r1 * r1 + r2 * r2 + r3 * r3 + r4 * r4;
    }

    for (; loffset < endL; roffset++, loffset++){
      final double r1 = larray[loffset] - rarray[roffset];
      result += r1 * r1;
    }
    return result;
  }

  public static void assign(final double[] larray, final int loffset, final double[] rarray, final int roffset, final int count) {
    final int alignedCount = (count / 4) * 4;
    for (int i = 0; i < alignedCount; i+=4) {
      larray[i + loffset] = rarray[i + roffset];
      larray[i + loffset + 1] = rarray[i + roffset + 1];
      larray[i + loffset + 2] = rarray[i + roffset + 2];
      larray[i + loffset + 3] = rarray[i + roffset + 3];
    }

    for (int i = alignedCount; i < count; i++){
      larray[i + loffset] = rarray[i + roffset];
    }
  }

  public static <F> F[] toArray(final Collection<F> weakModels) {
    if (weakModels.size() == 0)
      throw new IllegalArgumentException("Can create array");
    //noinspection unchecked
    return weakModels.toArray((F[])(weakModels.size() > 0 ? Array.newInstance(weakModels.iterator().next().getClass(), weakModels.size()) : new Object[0]));
  }

  public static <T extends Comparable> T max(final Seq<T> target) {
    if (target.length() == 0)
      throw new IllegalArgumentException("Empty sequence");
    T result = target.at(0);
    for (int i = 1; i < target.length(); i++)
      if(result.compareTo(target.at(i)) < 0)
        result = target.at(i);
    return result;
  }

  public static <T> int entriesCount(final Seq<T> labels, final T x) {
    int counter = 0;
    for (int i = 0; i < labels.length(); i++) {
      if (labels.at(i).equals(x))
        counter++;
    }
    return counter;
  }

  public static <I> I[] cut(final I[] data, final int[] indices) {
    if (indices.length == 0 || data.length == 0)
      throw new IllegalArgumentException();
    final I[] result = (I[])Array.newInstance(data[0].getClass(), indices.length);
    for (int i = 0; i < indices.length; i++) {
      result[i] = data[indices[i]];
    }
    return result;
  }

  public static <I> List<I> cut(final List<I> data, final int[] indices) {
    final List<I> result = new ArrayList<>(indices.length);
    for (int i = 0; i < indices.length; i++) {
      result.add(data.get(indices[i]));
    }
    return result;
  }

  public static <I> Seq<I> cut(final Seq<I> data, final int[] indices) {
    if (indices.length == 0 || data.length() == 0)
      throw new IllegalArgumentException();
    if (data instanceof SparseVec) {
      return (Seq<I>) VecTools.cutSparseVec((SparseVec) data, indices);
    }
    else if (data instanceof Vec) {
      final Vec dataVec = (Vec) data;
      final Vec result = new ArrayVec(indices.length);
      for (int i = 0; i < indices.length; i++) {
        result.set(i, dataVec.get(indices[i]));
      }
      return (Seq<I>) result;
    }
    else if (data instanceof IntSeq) {
      final IntSeq dataIntSeq = (IntSeq) data;
      final int[] ints = new int[indices.length];
      for (int i = 0; i < indices.length; i++) {
        ints[i] = dataIntSeq.arr[indices[i]];
      }
      return (Seq<I>) new IntSeq(ints);
    }
    else if (data instanceof VecSeq) {
      final VecSeq vecSeq = (VecSeq) data;
      final Vec[] cutVecs = new Vec[indices.length];
      for (int i = 0; i < indices.length; i++) {
        cutVecs[i] = vecSeq.at(indices[i]);
      }
      return (Seq<I>) new VecSeq(cutVecs);
    } else {
      final I[] result = (I[]) Array.newInstance(data.at(0).getClass(), indices.length);
      for (int i = 0; i < indices.length; i++) {
        result[i] = data.at(indices[i]);
      }
      return new ArraySeq<I>(result);
    }
  }

  public static <I> Seq<I> concat(final Seq<I>... seqs) {
    if (seqs.length == 0) {
      throw new IllegalArgumentException();
    }

    final SeqBuilder seqBuilder;
    final Seq<I> example = seqs[0];
    if (example instanceof Vec) {
      seqBuilder = new VecBuilder();
    }
    else if (example instanceof IntSeq) {
      seqBuilder = new IntSeqBuilder();
    }
    else {
      seqBuilder = new ArraySeqBuilder(example.at(0).getClass());
    }

    for (final Seq<I> seq : seqs) {
      seqBuilder.addAll(seq);
    }
    return seqBuilder.build();
  }

  public static int sum(final int[] arr, final int from, final int end) {
    int result = 0;
    for (int i = from; i < end; i++) {
      result += arr[i];
    }
    return result;
  }

  public static int sum(final int[] arr) {
    return sum(arr, 0, arr.length);
  }

  public static int[] fill(final int[] arr, final int val) {
    final int alignedCount = (arr.length / 4) * 4;
    for (int i = 0; i < alignedCount; i+=4) {
      arr[i] = val;
      arr[i + 1] = val;
      arr[i + 2] = val;
      arr[i + 3] = val;
    }

    for (int i = alignedCount; i < arr.length; i++){
      arr[i] = val;
    }
    return arr;
  }

  public static double[] fill(final double[] arr, final double val) {
    final int alignedCount = (arr.length / 4) * 4;
    for (int i = 0; i < alignedCount; i+=4) {
      arr[i] = val;
      arr[i + 1] = val;
      arr[i + 2] = val;
      arr[i + 3] = val;
    }

    for (int i = alignedCount; i < arr.length; i++){
      arr[i] = val;
    }
    return arr;
  }


  public static Object repack(final Object[] arr, final Class clazz) {
    final Object repack = Array.newInstance(clazz, arr.length);
    for (int i = 0; i < arr.length; i++) {
      Array.set(repack, i, arr[i]);
    }
    return repack;
  }

  public static boolean isSorted(final int[] a) {
    for (int i = 0; i < a.length - 1; i++) {
      if (a[i] > a[i + 1])
        return false;
    }
    return true;
  }

  public static <T> int indexOf(final T instance, final T[] array) {
    if (instance != null) {
      for(int i = 0; i < array.length; i++) {
        if (instance.equals(array[i]))
          return i;
      }
    }
    else {
      for(int i = 0; i < array.length; i++) {
        if (array[i] == null)
          return i;
      }
    }
    return -1;
  }

  public static <T> T[] concat(final T[] left, final T[] right) {
    @SuppressWarnings("unchecked")
    final T[] result = (T[])Array.newInstance(left.getClass().getComponentType(), left.length + right.length);
    System.arraycopy(left, 0, result, 0, left.length);
    System.arraycopy(right, 0, result, left.length, right.length);
    return result;
  }

  public static <T> boolean and(T[] input, Filter<? super T> filter) {
    for(int i = 0; i < input.length; i++) {
      if (!filter.accept(input[i]))
        return false;
    }
    return true;
  }

  public static <T> boolean or(T[] input, Filter<? super T> filter) {
    for(int i = 0; i < input.length; i++) {
      if (filter.accept(input[i]))
        return true;
    }
    return false;
  }
}
