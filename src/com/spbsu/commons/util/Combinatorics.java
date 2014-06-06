package com.spbsu.commons.util;

import com.spbsu.commons.math.MathTools;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * User: qdeee
 * Date: 29.05.14
 */
public class Combinatorics {
  public static interface Enumeration {
    boolean hasNext();
    int[] next();
    void skipN(long n);
    long totalCount();
  }

  public static class PartialPermutations implements Enumeration {
    private final int base;
    private final int positions;
    private int[] bitVec;
    private boolean isExhausted;

    //permutation will contain elements from {0, ... , base-1}
    public PartialPermutations(int base, int positions) {
      this.positions = positions;
      this.bitVec = new int[positions];
      this.base = base;
    }

    @Override
    public boolean hasNext() {
      return !isExhausted;
    }

    @Override
    public int[] next() {
      int[] copy = Arrays.copyOf(bitVec, bitVec.length);
      gotoNext();
      return copy;
    }

    @Override
    public void skipN(final long n) {
      for (int i = 0; i < n; i++) {
        gotoNext();
      }
    }

    @Override
    public long totalCount() {
      return (long)Math.pow(base, positions);
    }

    private void gotoNext() {
      int end = positions - 1;
      while (++bitVec[end] == base) {
        end--;
        if (end == -1) {
          isExhausted = true;
          break;
        }
        for (int j = end + 1; j < positions; j++) {
          bitVec[j] = 0;
        }
      }
    }
  }

  public static void main(String[] args) {
    final Permutations permutations = new Permutations(3);
    while (permutations.hasNext()) {
      System.out.println(Arrays.toString(permutations.next()));
    }
  }

  public static class Permutations implements Enumeration {
    private final int positions;
    private int[] bitVec;
    private boolean isExhausted;

    public Permutations(int base) {
      this.positions = base;
      this.bitVec = new int[base];

      for (int i = 0; i < bitVec.length; i++) {
        bitVec[i] = i;
      }
    }

    @Override
    public boolean hasNext() {
      return !isExhausted;
    }

    @Override
    public int[] next() {
      int[] copy = Arrays.copyOf(bitVec, bitVec.length);
      gotoNext();
      return copy;
    }

    private void gotoNext() {
      int end = positions - 1;
      while (isNumInArray(bitVec, ++bitVec[end], 0, end) || bitVec[end] == positions) {
        if (bitVec[end] == positions) {
          end--;
        }
        if (end == -1) {
          isExhausted = true;
          break;
        }
      }
      if (!isExhausted) {
        for (int i = end + 1; i < positions; i++) {
          for (int j = 0; j < positions; j++) {
            if (!isNumInArray(bitVec, j, 0, i)) {
              bitVec[i] = j;
              break;
            }
          }
        }
      }
    }

    @Override
    public void skipN(final long n) {
      for (int i = 0; i < n; i++) {
        gotoNext();
      }
    }

    @Override
    public long totalCount() {
      return MathTools.factorial(positions);
    }

    private boolean isNumInArray(int[] arr, int num, int start, int end) {
      for(int i = start; i < end; i++) {
        if(arr[i] == num) {
          return true;
        }
      }
      return false;
    }
  }
}
