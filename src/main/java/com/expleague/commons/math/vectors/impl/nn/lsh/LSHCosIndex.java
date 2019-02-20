package com.expleague.commons.math.vectors.impl.nn.lsh;

import com.expleague.commons.func.Functions;
import com.expleague.commons.func.HashFunction;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.nn.NearestNeighbourIndex;
import com.expleague.commons.math.vectors.impl.nn.impl.EntryImpl;
import com.expleague.commons.random.FastRandom;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LSHCosIndex implements NearestNeighbourIndex {
  public static final int HASHES_COUNT = 64;
  private final HashFunction<Vec>[] hashes;
  private final TLongArrayList sketches = new TLongArrayList();
  private final TLongArrayList ids = new TLongArrayList();
  private final List<Vec> vecs = new ArrayList<>();
  private final int dim;
  private final int minDiff;

  public LSHCosIndex(FastRandom rng, int minDiff, int dim) {
    this.dim = dim;
    this.minDiff = minDiff;
    this.hashes = IntStream.range(0, HASHES_COUNT).mapToObj(i -> new CosDistanceHashFunction(dim, rng)).<HashFunction<Vec>>toArray(HashFunction[]::new);
  }

  @Override
  public int dim() {
    return dim;
  }

  @Override
  public Stream<Entry> nearest(Vec query) {
    final double queryNorm = VecTools.norm(query);
    final long qhash = sketch(query);
    return IntStream.range(minDiff, hashes.length + 1).mapToObj(diff ->
        resultsWithDiff(qhash, diff, diff == minDiff)
            .mapToObj(idx -> {
              final Vec vec = vecs.get(idx);
              return new EntryImpl(idx, ids.getQuick(idx), vec, (1 - VecTools.multiply(query, vec) / queryNorm) / 2);
            })
            .sorted(EntryImpl::compareTo)
            .map(Functions.cast(Entry.class))
    ).flatMap(Function.identity());
  }

  private IntStream resultsWithDiff(long qhash, int diff, boolean less) {
    if (less)
      return IntStream.range(0, sketches.size()).parallel()
          .filter(i -> Long.bitCount(qhash ^ sketches.getQuick(i)) <= diff);
    return IntStream.range(0, sketches.size()).parallel()
        .filter(i -> Long.bitCount(qhash ^ sketches.getQuick(i)) == diff);
  }

  public long sketch(Vec query) {
    long qhash = 0;
    long bit = 1;
    for (int i = 0; i < hashes.length; i++, bit <<= 1) {
      if (hashes[i].hash(query) > 0)
        qhash |= bit;
    }
    return qhash;
  }

  @Override
  public synchronized void append(long id, Vec vec) {
    sketches.add(sketch(vec));
    ids.add(id);
    vecs.add(vec);
  }

  @Override
  public synchronized void remove(long id) {
    final int index = ids.indexOf(id);
    ids.remove(index, 1);
    sketches.remove(index, 1);
    vecs.remove(index);
  }
}
