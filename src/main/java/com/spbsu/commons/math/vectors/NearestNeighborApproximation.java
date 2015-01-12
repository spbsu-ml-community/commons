package com.spbsu.commons.math.vectors;

import java.util.List;

/**
* User: solar
* Date: 13.08.12
* Time: 18:14
*/
public class NearestNeighborApproximation {
  public static final int NN_COUNT = 50;
  private final LSHEuclidNNLocator locator;
  private final double[] target;

  public NearestNeighborApproximation(final List<Vec> data, final double[] target) {
    this.locator = new LSHEuclidNNLocator(data, 50, data.size()/50);
    this.target = target;
  }

  public double get(final Vec vec) {
    final int[] nearest = new int[NN_COUNT];
    final double[] distance = new double[NN_COUNT];
    final int count = locator.nearest(vec, NN_COUNT, nearest, distance);
    double avg = 0;
    double totalWeight = 0;
    for (int i = 0; i < count; i++) {
      final double weight = 1/(distance[i] + 0.01);
      avg += target[nearest[i]] * weight;
      totalWeight += weight;
    }
    return avg / totalWeight;
  }
}
