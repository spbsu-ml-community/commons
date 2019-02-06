package com.expleague.commons.math.vectors.impl.nn.lsh;

import com.expleague.commons.func.Functions;
import com.expleague.commons.func.HashFunction;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.nn.NearestNeighbourIndex;
import com.expleague.commons.math.vectors.impl.nn.impl.EntryImpl;
import com.expleague.commons.random.FastRandom;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class BaseQuantLSHCosIndex implements NearestNeighbourIndex {
  public static final int SKETCH_BITS_PER_QUANT = 32;
  protected final HashFunction<Vec>[] hashes;
  protected final List<TIntArrayList> sketches;
  protected final TLongArrayList ids;

  private final int dim;
  protected final int minDist;

  public BaseQuantLSHCosIndex(FastRandom rng, int quantDim, int dim, int minDist) {
    this.dim = dim;
    this.minDist = minDist;
    this.ids = new TLongArrayList();
    this.sketches = new ArrayList<>();

    //noinspection unchecked
    this.hashes = (HashFunction<Vec>[]) new HashFunction[SKETCH_BITS_PER_QUANT * (int)Math.ceil(dim / (double)quantDim)];
    for (int i = 0; i < dim; i += quantDim) {
      int finalI = i;
      final int currentDim = Math.min(quantDim, dim - i);
      for (int b = 0; b < SKETCH_BITS_PER_QUANT; b++) {
        hashes[i / quantDim * SKETCH_BITS_PER_QUANT + b] = new CosDistanceHashFunction(currentDim, rng) { // quant sketch
          @Override
          public int hash(Vec v) {
            return super.hash(v.sub(finalI, currentDim));
          }
        };
      }
      sketches.add(new TIntArrayList());
    }
  }

  public BaseQuantLSHCosIndex(int dim, int minDist, TLongArrayList ids, List<TIntArrayList> sketches, HashFunction<Vec>[] hashes) {
    this.dim = dim;
    this.minDist = minDist;
    this.ids = ids;
    this.sketches = sketches;
    this.hashes = hashes;
  }

  @Override
  public int dim() {
    return dim;
  }

  private IntStream resultsWithDiff(int[] sketch, int diff, boolean less) {
    final int[] ints = IntStream.range(0, ids.size())
        .parallel()
        .filter(i -> {
          final int currentDiff = IntStream.range(0, sketch.length)
              .map(s -> Integer.bitCount(sketch[s] ^ sketches.get(s).getQuick(i)))
              .sum();
          return less ? currentDiff <= diff : currentDiff == diff;
        }).toArray();
    return IntStream.of(ints);
  }

  private int[] sketch(Vec x) {
    final int[] result = new int[hashes.length / SKETCH_BITS_PER_QUANT];
    for (int i = 0; i < result.length; i++) {
      int bit = 1;
      for (int b = 0; b < SKETCH_BITS_PER_QUANT; b++, bit <<= 1) {
        if (hashes[i * SKETCH_BITS_PER_QUANT + b].hash(x) > 0)
          result[i] |= bit;
      }
    }
    return result;
  }

  protected Stream<Entry> baseNearest(Vec query, IntFunction<Vec> getVec) {
    final double queryNorm = VecTools.norm(query);
    final int[] sketch = sketch(query);

    return IntStream.range(minDist, hashes.length + 1).mapToObj(diff ->
        resultsWithDiff(sketch, diff, diff == minDist)
            .mapToObj(idx -> {
              final Vec vec = getVec.apply(idx);
              return new EntryImpl(ids.getQuick(idx), vec, (1 - VecTools.multiply(query, vec) / queryNorm));
            })
            .sorted(EntryImpl::compareTo)
            .map(Functions.cast(Entry.class))
    ).flatMap(Function.identity());
  }

  protected synchronized void baseAppend(long id, Vec vec) {
    final int[] sketch = sketch(vec);
    ids.add(id);
    for (int i = 0; i < sketch.length; i++) {
      sketches.get(i).add(sketch[i]);
    }
  }

  protected synchronized int baseRemove(long id) {
    final int index = ids.indexOf(id);
    ids.remove(index, 1);
    for (TIntArrayList sketch : sketches) {
      sketch.remove(index, 1);
    }
    return index;
  }
}
