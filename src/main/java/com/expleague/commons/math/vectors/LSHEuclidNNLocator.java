package com.expleague.commons.math.vectors;

import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.util.ArrayTools;
import gnu.trove.set.hash.TIntHashSet;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * User: solar
 * Date: 26.07.12
 * Time: 20:20
 */
public class LSHEuclidNNLocator {
  private final List<Axis> axes = new ArrayList<Axis>();
  private final List<Vec> pool;

  private static class Axis {
    Vec a;
    double[] splits;
    int[] limits;
    int[] order;
  }

  public LSHEuclidNNLocator(final List<Vec> pool, final int size, final int parts) {
    this(pool, size, parts, new FastRandom());
  }

  public LSHEuclidNNLocator(final List<Vec> pool, final int size, final int parts, final Random random) {
    this.pool = pool;
    final int dim = pool.get(0).dim();
    final int poolSize = pool.size();
    for (int i = 0; i < size; i++) {
      final Axis axis = new Axis();
      axis.a = new ArrayVec(dim);
      for (int k = 0; k < dim; k++) {
        axis.a.set(k, random.nextGaussian());
      }
      final double[] products = new double[poolSize];
      for (int d = 0; d < poolSize; d++) {
        products[d] = VecTools.multiply(axis.a, pool.get(d));
      }
      axis.order = ArrayTools.sequence(0, poolSize);
      axis.limits = new int[parts];
      axis.splits = new double[parts];
      ArrayTools.parallelSort(products, axis.order);
//      double split = products[0];
//      double quant = (products[poolSize - 1] - split) / parts;
//      int index = 0;
//      int part = 0;
//      while (index < poolSize) {
//        split += quant;
//        axis.splits[part] = split;
//        while (index < poolSize && (products[index] <= split || part >= parts -1))
//          index++;
//        axis.limits[part] = index;
//        part++;
//      }
      int part = 0;
      final int quant = poolSize / parts;
      for (int j = 1; j < parts; j++) {
        int split = j * quant;
        final double value = products[split];
        while (split < poolSize && products[split] == value)
          split++;
        axis.splits[part] = value;
        axis.limits[part] = split;
        part++;
        while ((j + 1) * quant < split)
          j++;
      }
      axis.splits[parts - 1] = Double.MAX_VALUE;
      axes.add(axis);
    }
  }

  public int nearest(final Vec x, final int count, final int[] nearest, final double[] distance) {
    final int[] candidates;
    {
      final TIntHashSet found = new TIntHashSet(axes.size() * axes.get(0).splits.length);
      for (final Axis axis : axes) {
        final double product = VecTools.multiply(axis.a, x);
        int split = Arrays.binarySearch(axis.splits, product);
        split = split >= 0 ? split : -split-1;
        int start = split > 0 ? axis.limits[split - 1] : 0;
        final int end = axis.limits[split];
        while (start < end)
          found.add(axis.order[start++]);
      }
      candidates = found.toArray();
    }
    final double[] distances = new double[candidates.length];
    for (int i = 0; i < distances.length; i++) {
      distances[i] = VecTools.distance(x, pool.get(candidates[i]));
    }
    ArrayTools.parallelSort(distances, candidates);
    final int rSize = Math.min(count, distances.length);
    for (int i = 0; i < rSize; i++) {
      nearest[i] = candidates[i];
      distance[i] = distances[i];
    }
    return rSize;
  }

  public int nearest(final Vec vec, final int size, final int[] result) {
    return nearest(vec, size, result, new double[size]);
  }

  public Vec[] nearest(final Vec x, final int count) {
    final int[] nearest = new int[count];
    final Vec[] result = new Vec[nearest(x, count, nearest, new double[count])];
    for (int i = 0; i < result.length; i++)
      result[i] = pool.get(nearest[i]);
    return result;
  }
}
