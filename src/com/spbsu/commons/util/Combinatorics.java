package com.spbsu.commons.util;

import java.util.Arrays;

/**
 * User: qdeee
 * Date: 29.05.14
 */
public class Combinatorics {
  public static interface Enumeration {
    boolean hasNext();
    int[] next();
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
      return copy;
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

      int end = positions - 1;
      while (isNumInArray(bitVec, ++bitVec[end], 0, end) || bitVec[end] == positions) {
        if (bitVec[end] == positions) {
          end--;
        }
        if (end == -1) {
          isExhausted = true;
          return copy;
        }
      }
      for (int i = end + 1; i < positions; i++) {
        for (int j = 0; j < positions; j++) {
          if (!isNumInArray(bitVec, j, 0, i)) {
            bitVec[i] = j;
            break;
          }
        }
      }
      return copy;
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
