package com.expleague.commons.math;

import com.expleague.commons.math.vectors.Distance;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.nn.NearestNeighbourIndex;
import com.expleague.commons.math.vectors.impl.nn.faiss.Faiss;
import com.expleague.commons.math.vectors.impl.nn.naive.NaiveNNIndex;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.util.logging.Interval;
import gnu.trove.set.hash.TLongHashSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class KMeansTest {
  @Test
  public void testKMeansSingleCentroid() {
    FastRandom rng = new FastRandom(100500);
    final Vec[] centroids = IntStream.range(0, 1)
        .mapToObj(idx -> new ArrayVec(10))
        .peek(v -> VecTools.fillGaussian(v, rng))
        .toArray(Vec[]::new);
    Vec sum = new ArrayVec(centroids[0].dim());
    int[] count = new int[]{0};
    final Vec[] answer = VecTools.kMeans(centroids.length, 1, rng, IntStream.range(0, 10000).mapToObj(i -> {
      final ArrayVec next = VecTools.append(VecTools.fillGaussian(new ArrayVec(centroids[0].dim()), rng), centroids[rng.nextInt(centroids.length)]);
      VecTools.append(sum, next);
      count[0]++;
      return next;
    }));

    VecTools.scale(sum, 1./count[0]);

    double totalDist = 0;
    for (final Vec q : answer) {
      final double minDist = Stream.of(centroids).mapToDouble(v -> VecTools.distance(q, v)).min().orElse(Double.POSITIVE_INFINITY);
      totalDist += minDist;
    }
    Assert.assertTrue("Mean residual: " + (totalDist / centroids.length) + " while mean diff is: " + VecTools.distance(centroids[0], sum), totalDist < centroids[0].dim() * 1e-2);
  }

  @Test
  public void testKMeansNoNNErrors() {
    FastRandom rng = new FastRandom(100500);
    final Vec[] centroids = IntStream.range(0, 2)
        .mapToObj(idx -> new ArrayVec(10))
        .peek(v -> VecTools.fillGaussian(v, rng))
        .peek(v -> VecTools.scale(v, 10))
        .toArray(Vec[]::new);
    final Vec[] answer = VecTools.kMeans(centroids.length, 2, rng, IntStream.range(0, 100000).mapToObj(i ->
        VecTools.append(VecTools.fillGaussian(new ArrayVec(centroids[0].dim()), rng), centroids[rng.nextInt(centroids.length)]))
    );

    double totalDist = 0;
    for (final Vec q : answer) {
      final double minDist = Stream.of(centroids).mapToDouble(v -> VecTools.distance(q, v)).min().orElse(Double.POSITIVE_INFINITY);
      totalDist += minDist;
    }
    Assert.assertTrue("Mean residual: " + (totalDist / centroids.length) + " while mean norm is: " + Stream.of(centroids).mapToDouble(VecTools::norm).average().orElse(Double.POSITIVE_INFINITY), totalDist / answer.length < centroids[0].dim() * 1e-2);
  }

  @Test
  public void testKMeansTen() {
    FastRandom rng = new FastRandom(100500);
    final Vec[] centroids = IntStream.range(0, 10)
        .mapToObj(idx -> new ArrayVec(10))
        .peek(v -> VecTools.fillGaussian(v, rng))
        .peek(v -> VecTools.scale(v, 10))
        .toArray(Vec[]::new);
    final Vec[] answer = VecTools.kMeans(centroids.length, 5, rng, IntStream.range(0, 100000).mapToObj(i ->
        VecTools.append(VecTools.fillGaussian(new ArrayVec(centroids[0].dim()), rng), centroids[rng.nextInt(centroids.length)]))
    );

    double totalDist = 0;
    for (final Vec q : answer) {
      final double minDist = Stream.of(centroids).mapToDouble(v -> VecTools.distance(q, v)).min().orElse(Double.POSITIVE_INFINITY);
      totalDist += minDist;
    }
    Assert.assertTrue("Mean residual: " + (totalDist / centroids.length) + " while mean norm is: " + Stream.of(centroids).mapToDouble(VecTools::norm).average().orElse(Double.POSITIVE_INFINITY), totalDist / answer.length < centroids[0].dim() * 1e-2);
  }

  @Test
  public void testKMeansUniform() {
    final FastRandom rng = new FastRandom(100500);
    final Vec[] vecs = IntStream.range(0, 100000)
        .mapToObj(i -> VecTools.fillUniform(new ArrayVec(100), rng))
        .toArray(Vec[]::new);
    final Vec[] answer = VecTools.kMeans(10, 10, rng, IntStream.range(0, 1000000).mapToObj(i -> vecs[rng.nextInt(vecs.length)]));
    System.out.println();
  }
//  @Test
  public void testKMeansHundred() {
    final FastRandom rng = new FastRandom(100500);
    final Vec[] centroids = IntStream.range(0, 1024)
        .mapToObj(idx -> new ArrayVec(10))
        .peek(v -> VecTools.fillGaussian(v, rng))
        .peek(v -> VecTools.scale(v, 10))
        .toArray(Vec[]::new);
    Interval.start();
    final Vec[] answer = VecTools.kMeans(centroids.length, 15, rng, IntStream.range(0, 1000000).mapToObj(i ->
        VecTools.append(VecTools.fillGaussian(new ArrayVec(centroids[0].dim()), rng), centroids[rng.nextInt(centroids.length)]))
    );
    Interval.stopAndPrint();

    double totalDist = 0;
    for (final Vec q : answer) {
      final double minDist = Stream.of(centroids).mapToDouble(v -> VecTools.distance(q, v)).min().orElse(Double.POSITIVE_INFINITY);
      totalDist += minDist;
    }
    final double v = totalDist / centroids.length;
    Assert.assertTrue("Mean residual: " + v + " while mean norm is: " + Stream.of(centroids).mapToDouble(VecTools::norm).average().orElse(Double.POSITIVE_INFINITY), v < centroids[0].dim() * 5e-1);
  }
}
