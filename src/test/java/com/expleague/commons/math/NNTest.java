package com.expleague.commons.math;

import com.expleague.commons.math.vectors.Distance;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.nn.NearestNeighbourIndex;
import com.expleague.commons.math.vectors.impl.nn.lsh.LSHCosIndex;
import com.expleague.commons.math.vectors.impl.nn.lsh.BaseQuantLSHCosIndex;
import com.expleague.commons.math.vectors.impl.nn.lsh.QuantLSHCosIndexRAM;
import com.expleague.commons.math.vectors.impl.nn.naive.NaiveNNIndex;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.util.logging.Interval;
import gnu.trove.set.hash.TLongHashSet;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.IIOException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NNTest {
  public static final int DIM = 100;
  public static final int NN_SIZE = 1;

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
  public void testQuantLSHCos() throws IOException {
    final NearestNeighbourIndex naive = new NaiveNNIndex(Distance.COS, DIM);
    final NearestNeighbourIndex lsh = new QuantLSHCosIndexRAM(rng,10, DIM, 130);

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
//        System.out.println(e.distance() + " " + Long.toBinaryString(lsh.sketch(e.vec()) ^ lsh.sketch(query)));
//      });
      long found = naive.nearest(query).limit(NN_SIZE)
          .mapToLong(NearestNeighbourIndex.Entry::id).filter(lshAnswer::contains).count();
      total += found / (double)NN_SIZE;
    }

    Interval.stopAndPrint("LSH precision: " + (total / tries));
    Assert.assertTrue("Current LSH precision: " + (total / tries), total / tries > 0.7);
  }
}
