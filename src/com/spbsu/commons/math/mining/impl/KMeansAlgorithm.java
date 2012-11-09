package com.spbsu.commons.math.mining.impl;

import com.spbsu.commons.func.Computable;
import com.spbsu.commons.math.mining.ClusterizationAlgorithm;
import com.spbsu.commons.math.vectors.impl.SparseVec;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.VecTools;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * User: solar
 * Date: 13.02.2010
 * Time: 22:49:47
 */
public class KMeansAlgorithm<T> implements ClusterizationAlgorithm<T> {
  int clustCount;
  double maxDist;

  public KMeansAlgorithm(int clustCount, double maxDist) {
    this.clustCount = clustCount;
    this.maxDist = maxDist;
  }

  @NotNull
  @Override
  public Collection<? extends Collection<T>> cluster(Collection<T> dataSet, Computable<T, Vec> data2DVector) {
    Vec[] centroids = new Vec[clustCount];
    List<Set<T>> clusters = new ArrayList<Set<T>>();
    while (clusters.size() < centroids.length) {
      clusters.add(new HashSet<T>());
    }
    int fullIndex = 0;
    for (T point : dataSet) {
      Vec vec = data2DVector.compute(point);
      int index = fullIndex++ % centroids.length;
      if (centroids[index] == null)
        //noinspection unchecked
        centroids[index] = new SparseVec(vec.basis());
      VecTools.append(centroids[index], vec);
      clusters.get(index).add(point);
    }

    int iteration = 0;
    do {
      final Vec[] nextCentroids = new Vec[clustCount];
      for (int i = 0; i < centroids.length; i++) {
        //noinspection unchecked
        nextCentroids[i] = new SparseVec(centroids[i].basis());
        final Set<T> cluster = clusters.get(i);
        final VecIterator centIter = centroids[i].nonZeroes();
        final int scale = Math.max(cluster.size(), 1);
        while (centIter.advance()) {
          final double v = centIter.value() / scale;
          centIter.setValue(v < 0.01 ? 0 : v);
        }
        cluster.clear();
      }

      for (T point : dataSet) {
        final Vec vec = data2DVector.compute(point);
        double minResemblance = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < centroids.length; i++) {
          final Vec centroid = centroids[i];
          final double resemblance = VecTools.distanceAV(centroid, vec);
          if (resemblance < minResemblance) {
            minResemblance = resemblance;
            minIndex = i;
          }
        }
        clusters.get(minIndex).add(point);
        VecTools.append(nextCentroids[minIndex], vec);
      }

      centroids = nextCentroids;
    }
    while (++iteration < 10);

    final Iterator<Set<T>> iter = clusters.iterator();
    int index = 0;
    while (iter.hasNext()) {
      final Set<T> cluster = iter.next();
      double meanDist = 0;
      Vec centroid = centroids[index++];
      for (T term : cluster) {
        meanDist += VecTools.distanceAV(data2DVector.compute(term), centroid);
      }
      meanDist /= cluster.size();
      if (meanDist > maxDist) {
//        iter.remove();
      }
    }

    return clusters;
  }
}
