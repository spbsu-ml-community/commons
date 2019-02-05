package com.expleague.commons.math.vectors.impl.nn.lsh;

import com.expleague.commons.func.Functions;
import com.expleague.commons.func.HashFunction;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.nn.NearestNeighbourIndex;
import com.expleague.commons.math.vectors.impl.nn.impl.EntryImpl;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.random.FastRandom;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LSHCosIndex implements NearestNeighbourIndex {
  private final HashFunction<Vec>[] hashes;
  private final TLongObjectHashMap<TLongObjectHashMap<Vec>> buckets = new TLongObjectHashMap<>();
  private final int dim;
  private final int minDiff;

  public LSHCosIndex(FastRandom rng, int minDiff, int hashes, int dim) {
    this.dim = dim;
    this.minDiff = minDiff;
    this.hashes = IntStream.range(0, hashes).mapToObj(i -> new CosDistanceHashFunction(dim, rng)).<HashFunction<Vec>>toArray(HashFunction[]::new);
  }

  @Override
  public int dim() {
    return dim;
  }

  @Override
  public Stream<Entry> nearest(Vec query) {
    final double queryNorm = VecTools.norm(query);
    final long qhash = hash(query);
    return IntStream.range(minDiff, hashes.length + 1).mapToObj(diff ->
        resultsWithDiff(qhash, diff).stream()
            .peek(entry -> entry.setDistance((1 - VecTools.multiply(query, entry.vec()) / queryNorm) / 2))
            .sorted(EntryImpl::compareTo)
            .map(Functions.cast(Entry.class))
    ).flatMap(Function.identity());
  }

  protected Stream<EntryImpl> resultsWithDiff(Vec query, int diff) {
    final long qhash = hash(query);
    return resultsWithDiff(qhash, diff).stream();
  }

  private List<EntryImpl> resultsWithDiff(long qhash, int diff) {
    final List<EntryImpl> result = new ArrayList<>();
    buckets.forEachEntry((bucketId, bucket) -> {
      final int dist = Long.bitCount(bucketId ^ qhash);
      if (dist > diff || (diff != minDiff && dist < diff))
        return true;
      bucket.forEachEntry((id, vec) -> {
        result.add(new EntryImpl(id, vec, dist));
        return true;
      });
      return true;
    });
    return result;
  }

  public long hash(Vec query) {
    long qhash = 0;
    long bit = 1;
    for (int i = 0; i < hashes.length; i++, bit <<= 1) {
      if (hashes[i].hash(query) > 0)
        qhash |= bit;
    }
    return qhash;
  }

  @Override
  public void append(long id, Vec vec) {
    final long hash = hash(vec);
    TLongObjectHashMap<Vec> bucket = buckets.get(hash);
    if (bucket == null)
      buckets.put(hash, bucket = new TLongObjectHashMap<>());
    bucket.put(id, VecTools.normalizeL2(VecTools.copy(vec)));
  }

  @Override
  public void remove(long id) {
    buckets.forEachValue(bucket -> bucket.remove(id) == null);
  }

  private static class CosDistanceHashFunction implements HashFunction<Vec> {
    Vec w;

    CosDistanceHashFunction(int dim, FastRandom rng) {
      w = VecTools.fillGaussian(new ArrayVec(dim), rng);
    }

    @Override
    public int hash(Vec v) {
      return VecTools.multiply(v, w) > 0 ? 0 : 1;
    }

    @Override
    public int bits() {
      return 1;
    }
  }
}
