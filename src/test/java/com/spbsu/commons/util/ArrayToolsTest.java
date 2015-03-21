package com.spbsu.commons.util;

import com.spbsu.commons.random.FastRandom;
import junit.framework.TestCase;

/**
 * Created by noxoomo on 21/03/15.
 */
public class ArrayToolsTest extends TestCase {
  FastRandom rand = new FastRandom(42);
  static int sizes[] = {2, 3, 5, 7, 11, 15, 100, 971};
  static int tries = 10000;

  public static double[] dsequence(final int start, final int end) {
    final double[] result = new double[end - start];
    for (int i = 0; i < result.length; i++) {
      result[i] = i + start;
    }
    return result;
  }

  public void testParallelSortIntInt() {
    for (int tr = 0; tr < tries; ++tr)
      for (int size : sizes) {
        int[] arr = new int[size];
        int[] linked;
        for (int i = 0; i < arr.length; ++i) {
          arr[i] = rand.nextInt(5 * (tr + 1));
        }
        linked = ArrayTools.sequence(0, arr.length);
        int[] arrCopy = arr.clone();
        ArrayTools.parallelSort(arr, linked);
        for (int i = 1; i < arr.length; ++i) {
          assertTrue(arr[i - 1] <= arr[i]);
          assertTrue(arr[i] == arrCopy[linked[i]]);
        }
      }
  }

  public void testParallelSortIntDouble() {
    for (int tr = 0; tr < tries; ++tr)
      for (int size : sizes) {
        int[] arr = new int[size];
        double[] linked;
        for (int i = 0; i < arr.length; ++i) {
          arr[i] = rand.nextInt(5 * (tr + 1));
        }
        linked = dsequence(0, arr.length);
        int[] arrCopy = arr.clone();
        ArrayTools.parallelSort(arr, linked);
        for (int i = 1; i < arr.length; ++i) {
          assertTrue(arr[i - 1] <= arr[i]);
          assertTrue(arr[i] == arrCopy[((int) linked[i])]);
        }
      }
  }


  public void testParallelSortDoubleInt() {
    for (int tr = 0; tr < tries; ++tr)
      for (int size : sizes) {
        double[] arr = new double[size];
        int[] linked;
        for (int i = 0; i < arr.length; ++i) {
          arr[i] = rand.nextDouble();
        }
        linked = ArrayTools.sequence(0, arr.length);
        double[] arrCopy = arr.clone();
        ArrayTools.parallelSort(arr, linked);
        for (int i = 1; i < arr.length; ++i) {
          assertTrue(arr[i - 1] <= arr[i]);
          assertTrue(arr[i] == arrCopy[(linked[i])]);
        }
      }
  }

  public void testParallelSortLongInt() {
    for (int tr = 0; tr < tries; ++tr)
      for (int size : sizes) {
        long[] arr = new long[size];
        int[] linked;
        for (int i = 0; i < arr.length; ++i) {
          arr[i] = rand.nextInt(5 * (tr + 1));
        }
        linked = ArrayTools.sequence(0, arr.length);
        long[] arrCopy = arr.clone();
        ArrayTools.parallelSort(arr, linked);
        for (int i = 1; i < arr.length; ++i) {
          assertTrue(arr[i - 1] <= arr[i]);
          assertTrue(arr[i] == arrCopy[linked[i]]);
        }
      }
  }

  public void testParallelSortDoubleDoubleDouble() {
    for (int tr = 0; tr < tries; ++tr)
      for (int size : sizes) {
        double[] arr = new double[size];
        double[] linked;
        double[] linked2;
        for (int i = 0; i < arr.length; ++i) {
          arr[i] = rand.nextDouble();
        }
        linked = dsequence(0, arr.length);
        linked2 = new double[linked.length];
        for (int j = 0; j < linked.length; ++j) {
          linked2[j] = linked.length - j - 1;
        }
        double[] arrCopy = arr.clone();
        ArrayTools.parallelSort(arr, linked, linked2);
        for (int i = 1; i < arr.length; ++i) {
          assertTrue(arr[i - 1] <= arr[i]);
          assertTrue(arr[i] == arrCopy[((int) linked[i])]);
          assertTrue(arr[i] == arrCopy[((int) (arr.length - linked2[i] - 1))]);
        }
      }
  }


}
