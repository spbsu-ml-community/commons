package com.expleague.commons.math;

import com.expleague.commons.math.vectors.Distance;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.nn.NearestNeighbourIndex;
import com.expleague.commons.math.vectors.impl.nn.faiss.Faiss;
import com.expleague.commons.math.vectors.impl.nn.lsh.LSHCosIndex;
import com.expleague.commons.math.vectors.impl.nn.lsh.QuantLSHCosIndexRAM;
import com.expleague.commons.math.vectors.impl.nn.naive.NaiveNNIndex;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.util.logging.Interval;
import gnu.trove.set.hash.TLongHashSet;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class NNTest {
  public static final int DIM = 100;
  public static final int NN_SIZE = 1;
  private static final int BATCH_SIZE = 500;


  private final FastRandom rng = new FastRandom(0);

  @Test
  public void testLSHCos() {
    final NearestNeighbourIndex naive = new NaiveNNIndex(Distance.COS, DIM);
    final LSHCosIndex lsh = new LSHCosIndex(rng, 24, DIM);

    for (int i = 0; i < 100000; i++) {
      Vec v = VecTools.fillUniform(new ArrayVec(DIM), rng);
      naive.append(i, v);
      lsh.append(i, v);
    }

    double total = 0;
    final int tries = 100;
    Interval.start();
    Interval.suspend();
    for (int i = 0; i < tries; i++) {
      Vec query = VecTools.fillUniform(new ArrayVec(DIM), rng);
      Interval.resume();
      final TLongHashSet lshAnswer = new TLongHashSet(lsh.nearest(query).limit(NN_SIZE * 100)
          .mapToLong(NearestNeighbourIndex.Entry::id).toArray());
      Interval.suspend();
//      naive.nearest(query).limit(10).forEach(e -> {
//        System.out.println(e.distance() + " " + Long.toBinaryString(lsh.hash(e.vec()) ^ lsh.hash(query)));
//      });
      long found = naive.nearest(query).limit(NN_SIZE)
          .mapToLong(NearestNeighbourIndex.Entry::id).filter(lshAnswer::contains).count();
      total += found / (double)NN_SIZE;
    }

    Interval.stopAndPrint("LSH precision: " + (total / tries));
    Assert.assertTrue("Current LSH precision: " + (total / tries), total / tries > 0.5);
  }

  @Test
  public void testQuantLSHCos() {
    final NearestNeighbourIndex naive = new NaiveNNIndex(Distance.COS, DIM);
    final NearestNeighbourIndex lsh = new QuantLSHCosIndexRAM(rng, DIM, 10, 32, 500);

    for (int i = 0; i < 100000; i++) {
      Vec v = VecTools.fillUniform(new ArrayVec(DIM), rng);
      naive.append(i, v);
      lsh.append(i, v);
    }

    double total = 0;
    final int tries = 100;
    Interval.start();
    Interval.suspend();
    for (int i = 0; i < tries; i++) {
      Vec query = VecTools.fillUniform(new ArrayVec(DIM), rng);
      Interval.resume();
      final TLongHashSet lshAnswer = new TLongHashSet(lsh.nearest(query).limit(NN_SIZE * 100)
          .mapToLong(NearestNeighbourIndex.Entry::id).toArray());
      Interval.suspend();
      long found = naive.nearest(query).limit(NN_SIZE)
          .mapToLong(NearestNeighbourIndex.Entry::id).filter(lshAnswer::contains).count();
      total += found / (double)NN_SIZE;
    }

    Interval.stopAndPrint("LSH precision: " + (total / tries));
    Assert.assertTrue("Current LSH precision: " + (total / tries), total / tries > 0.7);
  }

  private static final int PART_DIM = 10;
  private static final int AREA_CENTROIDS_NUM = 30;
  private static final int PART_CENTROIDS_NUM = 60;

  @Ignore
  @Test
  public void faissTest() {
    final NearestNeighbourIndex naive = new NaiveNNIndex(Distance.L2, DIM);
    final Faiss faiss = new Faiss(DIM, PART_DIM, BATCH_SIZE, AREA_CENTROIDS_NUM, PART_CENTROIDS_NUM);

    for (int i = 0; i < 10000; i++) {
      Vec v = VecTools.fillUniform(new ArrayVec(DIM), rng);
      naive.append(i, v);
      faiss.append(i, v);
    }
    faiss.build();

    double total = 0;
    final int tries = 100;
    Interval.start();
    Interval.suspend();
    for (int i = 0; i < tries; i++) {
      Vec query = VecTools.fillUniform(new ArrayVec(DIM), rng);
      Interval.resume();
      final TLongHashSet faissAnswer = new TLongHashSet(faiss.nearest(query).limit(NN_SIZE * 100)
              .mapToLong(NearestNeighbourIndex.Entry::id).toArray());
      Interval.suspend();
      long found = naive.nearest(query).limit(NN_SIZE)
              .mapToLong(NearestNeighbourIndex.Entry::id).filter(faissAnswer::contains).count();
      total += found / (double)NN_SIZE;
    }

    Interval.stopAndPrint("Faiss precision: " + (total / tries));
  }
}
