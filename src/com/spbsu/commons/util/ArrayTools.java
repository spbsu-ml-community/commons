package com.spbsu.commons.util;

import com.spbsu.commons.func.Computable;
import com.spbsu.commons.func.Evaluator;

import java.lang.reflect.Array;
import java.util.Arrays;

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

  public static void parallelSort(int[] a, double[] linked) {
    if(a.length != linked.length)
      throw new IllegalArgumentException("arrays sizes are not equal");
//      shuffle(a, linked); // to guard against worst-case
    parallelSort(a, linked, 0, a.length - 1);
  }

  public static void parallelSort(long[] a, int[] linked) {
    if(a.length != linked.length)
      throw new IllegalArgumentException("arrays sizes are not equal");
//      shuffle(a, linked); // to guard against worst-case
    parallelSort(a, linked, 0, a.length - 1);
  }

  // parallelSort a[left] to a[right]
  public static void parallelSort(int[] a, double[] linked, int left, int right) {
    if (right <= left) return;
    int i = partition(a, linked, left, right);
    parallelSort(a, linked, left, i-1);
    parallelSort(a, linked, i+1, right);
  }

  // parallelSort a[left] to a[right]
  public static void parallelSort(long[] a, int[] linked, int left, int right) {
    if (right <= left) return;
    int i = partition(a, linked, left, right);
    parallelSort(a, linked, left, i-1);
    parallelSort(a, linked, i+1, right);
  }

  // partition a[left] to a[right], assumes left < right
  private static int partition(int[] a, double[] linked, int left, int right) {
    int i = left - 1;
    int j = right;
    while (true) {
      while ((a[++i] < a[right]))      // find item on left to swap
        ;                               // a[right] acts as sentinel
      while ((a[right] < a[--j]))      // find item on right to swap
        if (j == left) break;           // don't go out-of-bounds
      if (i >= j) break;                  // check if pointers cross
      swap(a, linked, i, j);                      // swap two elements into place
    }
    swap(a, linked, i, right);                      // swap with partition element
    return i;
  }

  // partition a[left] to a[right], assumes left < right
  private static int partition(long[] a, int[] linked, int left, int right) {
    int i = left - 1;
    int j = right;
    while (true) {
      while ((a[++i] < a[right]))      // find item on left to swap
        ;                               // a[right] acts as sentinel
      while ((a[right] < a[--j]))      // find item on right to swap
        if (j == left) break;           // don't go out-of-bounds
      if (i >= j) break;                  // check if pointers cross
      swap(a, linked, i, j);                      // swap two elements into place
    }
    swap(a, linked, i, right);                      // swap with partition element
    return i;
  }

  public static void parallelSort(double[] a, int[] linked) {
    if(a.length != linked.length)
      throw new IllegalArgumentException("arrays sizes are not equal");
    parallelSort(a, linked, 0, a.length - 1);
  }

  public static void parallelSort(double[] a, int[] linked, int left, int right) {
    if (right <= left) return;
    int i = partition(a, linked, left, right);
    parallelSort(a, linked, left, i-1);
    parallelSort(a, linked, i+1, right);
  }

  // partition a[left] to a[right], assumes left < right
  private static int partition(double[] a, int[] linked, int left, int right) {
    int i = left - 1;
    int j = right;
    while (true) {
      while ((a[++i] < a[right]))      // find item on left to swap
        ;                               // a[right] acts as sentinel
      while ((a[right] < a[--j]))      // find item on right to swap
        if (j == left) break;           // don't go out-of-bounds
      if (i >= j) break;                  // check if pointers cross
      swap(a, linked, i, j);                      // swap two elements into place
    }
    swap(a, linked, i, right);                      // swap with partition element
    return i;
  }

  // exchange a[i] and a[j]
  private static void swap(double[] a, int[] linked, int i, int j) {
    {
      double swap = a[i];
      a[i] = a[j];
      a[j] = swap;
    }
    {
      int swap = linked[i];
      linked[i] = linked[j];
      linked[j] = swap;
    }
  }

  // exchange a[i] and a[j]
  private static void swap(int[] a, double[] linked, int i, int j) {
    {
      int swap = a[i];
      a[i] = a[j];
      a[j] = swap;
    }
    {
      double swap = linked[i];
      linked[i] = linked[j];
      linked[j] = swap;
    }
  }

  // exchange a[i] and a[j]
  private static void swap(long[] a, int[] linked, int i, int j) {
    {
      long swap = a[i];
      a[i] = a[j];
      a[j] = swap;
    }
    {
      int swap = linked[i];
      linked[i] = linked[j];
      linked[j] = swap;
    }
  }

  // shuffle the array a[]
  private static void shuffle(int[] a, double[] linked) {
    int N = a.length;
    for (int i = 0; i < N; i++) {
      int r = i + (int) (Math.random() * (N-i));   // between i and N-1
      swap(a, linked, i, r);
    }
  }

  // shuffle the array a[]
  private static void shuffle(long[] a, int[] linked) {
    int N = a.length;
    for (int i = 0; i < N; i++) {
      int r = i + (int) (Math.random() * (N-i));   // between i and N-1
      swap(a, linked, i, r);
    }
  }

  public static int[] sequence(int start, int end) {
    int[] result = new int[end - start];
    while (start < end) {
      result[start] = start;
      start++;
    }
    return result;
  }

  public static void parallelSort(int[] a, int[] linked) {
    if(a.length != linked.length)
      throw new IllegalArgumentException("arrays sizes are not equal");
//      shuffle(a, linked); // to guard against worst-case
    parallelSort(a, linked, 0, a.length - 1);
  }

  public static void parallelSort(int[] a, int[] linked, int left, int right) {
    if (right <= left) return;
    int i = partition(a, linked, left, right);
    parallelSort(a, linked, left, i-1);
    parallelSort(a, linked, i+1, right);
  }

  // partition a[left] to a[right], assumes left < right
  private static int partition(int[] a, int[] linked, int left, int right) {
    int i = left - 1;
    int j = right;
    while (true) {
      while ((a[++i] < a[right]))      // find item on left to swap
        ;                               // a[right] acts as sentinel
      while ((a[right] < a[--j]))      // find item on right to swap
        if (j == left) break;           // don't go out-of-bounds
      if (i >= j) break;                  // check if pointers cross
      swap(a, linked, i, j);                      // swap two elements into place
    }
    swap(a, linked, i, right);                      // swap with partition element
    return i;
  }

  // exchange a[i] and a[j]
  private static void swap(int[] a, int[] linked, int i, int j) {
    {
      int swap = a[i];
      a[i] = a[j];
      a[j] = swap;
    }
    {
      int swap = linked[i];
      linked[i] = linked[j];
      linked[j] = swap;
    }
  }

  public static int max(double[] arr) {
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

  public static int min(double[] arr) {
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

  public static <T> int max(T[] arr, Evaluator<T> evaluator) {
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

  public static <F, T> T[] map(F[] models, Class<T> clazz, Computable<F, T> computable) {
    T[] result = (T[]) Array.newInstance(clazz, models.length);
    for (int i = 0; i < models.length; i++)
      result[i] = computable.compute(models[i]);
    return result;
  }

  public static <T> double[] score(T[] dirs, Evaluator<T> evaluator) {
    double[] result = new double[dirs.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = evaluator.value(dirs[i]);
    }
    return result;
  }

  public static void add(double[] larray, int loffset, double[] rarray, int roffset, int count) {
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

  public static void fill(double[] array, int offset, int length, double val) {
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

  public static void mul(double[] array, int offset, int length, double val) {
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

  public static void scale(double[] larray, int loffset, double[] rarray, int roffset, int count) {
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

  public static double mul(double[] larray, int loffset, double[] rarray, int roffset, int count) {
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

  public static void incscale(double[] larray, int loffset, double[] result, int resultOffset, int count, double scale) {
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

  public static double l2(double[] larray, int loffset, double[] rarray, int roffset, int count) {
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

  public static void assign(double[] larray, int loffset, double[] rarray, int roffset, int count) {
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
}
