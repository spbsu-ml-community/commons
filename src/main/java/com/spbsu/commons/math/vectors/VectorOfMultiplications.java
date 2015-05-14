package com.spbsu.commons.math.vectors;

import com.spbsu.commons.math.MathTools;
import com.spbsu.commons.math.vectors.impl.vectors.ArrayVec;

/**
 * Created by towelenee on 5/14/15.
 * Vector of multiplication for regression purposes
 */
public class VectorOfMultiplications extends ArrayVec {
  public VectorOfMultiplications(double[] data, int dimensions)
  {
    super(getMultiplications(data, dimensions), 0, MathTools.combinationsWithRepetition(data.length, dimensions));
  }

  private static double[] getMultiplications(double[] data, int dimensions) {
    int[] index = new int[dimensions];
    final int size = MathTools.combinationsWithRepetition(data.length, dimensions);
    final double[] answer = new double[size];
    for (int i = 0; i < size; i++)
    {
      {
        double value = 1;
        for (int j = 0; j < index.length; j++)
          value *= data[index[j]];
        answer[i] = value;
      }
      generateNextDecreasingIndex(index);
    }
    if (hasNextDecreasingIndex(index, data.length))
      throw new RuntimeException("Bug in multiplication generation");
    return answer;
  }

  private static boolean hasNextDecreasingIndex(final int[] index, final int maxValue) {
    for (int i = 0; i < index.length; i++)
      if (index[i] != maxValue)
        return true;
    return false;
  }
  private static void generateNextDecreasingIndex(final int[] index)
  {
    int j = index.length - 1;
    while (j > 0 && (index[j] == index[j - 1]))
      j--;
    index[j]++;
    for (int i = j + 1; i < index.length; i++)
      index[i] = 0;
  }
}
