package com.spbsu.commons.math.vectors;

import com.spbsu.commons.math.MathTools;

/**
 * Created by towelenee on 5/14/15.
 * Vector of multiplication for regression purposes
 */
public class VectorOfMultiplicationsFactory{
  public int getDim() {
    return dim;
  }

  private final int dim;
  private final int[][] indexes;
  public VectorOfMultiplicationsFactory(int maxIndex, int dimensions)
  {
    dim = MathTools.combinationsWithRepetition(maxIndex, dimensions);
    indexes = new int[dim][];
    int[] index = new int[dimensions];
    for (int i = 0; i < dim; i++)
    {
      indexes[i] = index.clone();
      generateNextDecreasingIndex(index);
    }
    if (hasNextDecreasingIndex(index, maxIndex))
      throw new RuntimeException("Bug in multiplication generation");
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

  public double get(Vec data, int index) {
    double multiplication = 1;
    for(int i = 0; i < indexes[index].length; i++)
      multiplication *= data.get(indexes[index][i]);
    return multiplication;
  }
}
