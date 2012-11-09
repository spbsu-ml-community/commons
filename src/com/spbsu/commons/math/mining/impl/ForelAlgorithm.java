package com.spbsu.commons.math.mining.impl;

import com.spbsu.commons.func.Computable;
import com.spbsu.commons.math.mining.ClusterizationAlgorithm;
import com.spbsu.commons.math.vectors.impl.SparseVec;
import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.math.vectors.VecIterator;
import com.spbsu.commons.math.vectors.VecTools;
import com.spbsu.commons.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * User: solar
 * Date: 13.02.2010
 * Time: 20:32:44
 */
public class ForelAlgorithm<T> implements ClusterizationAlgorithm<T> {
  Logger LOG = Logger.create(ForelAlgorithm.class);
  private double maxDist0;

  public ForelAlgorithm(double maxDist0) {
    this.maxDist0 = maxDist0;
  }//  @Required

  @NotNull
  @Override
  public Collection<? extends Collection<T>> cluster(Collection<T> dataSet, Computable<T, Vec> data2DVector) {
    int count = 0;
    List<Set<T>> clusters = new ArrayList<Set<T>>();
    final Set<T> unclassified = new HashSet<T>(dataSet);
    while (!unclassified.isEmpty()) {
      final T first = unclassified.iterator().next();
      final Vec vec = data2DVector.compute(first);
      @SuppressWarnings({"unchecked"})
      final Set<T> cluster = new HashSet<T>();
      Vec centroid = VecTools.append(new SparseVec(vec.basis()), vec);
      int changesCount;
      double maxDist = maxDist0 + (1 - maxDist0) * Math.max(0, (1 - Math.log(1000) / Math.log(dataSet.size())));
      do {
        changesCount = 0;
        final Vec nextCentroid = VecTools.append(new SparseVec(vec.basis()), centroid);
        cluster.add(first);
        unclassified.remove(first);
        for (T currentTerm : unclassified) {
          final Vec currentVec = data2DVector.compute(currentTerm);
          final double distance = 1 - VecTools.cosine(centroid, currentVec);
          count ++;
          if (distance < maxDist && !cluster.contains(currentTerm)) {
            changesCount++;
            VecTools.scale(nextCentroid, cluster.size());
            VecTools.append(nextCentroid, currentVec);
            VecTools.scale(nextCentroid, 1./(cluster.size() + 1));
            cluster.add(currentTerm);
          }
          else if (distance >= maxDist && cluster.contains(currentTerm)) {
            changesCount++;
            VecTools.scale(nextCentroid, -cluster.size());
            VecTools.append(nextCentroid, currentVec);
            VecTools.scale(nextCentroid, -1./(cluster.size() - 1));
            cluster.remove(currentTerm);
          }
        }
        centroid = nextCentroid;
        final VecIterator iter = centroid.nonZeroes();
        while (iter.advance()) {
          if (iter.value() < 0.01)
            iter.setValue(0);
        }
        maxDist *= 0.99;
      }
      while (changesCount > 0);
      clusters.add(cluster);
      unclassified.removeAll(cluster);
    }
//    LOG.debug("Multiplications " + count + " for " + dataSet.size() + " objects");
    return clusters;
  }
}
