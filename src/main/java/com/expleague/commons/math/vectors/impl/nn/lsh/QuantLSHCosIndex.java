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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class QuantLSHCosIndex implements NearestNeighbourIndex {
  public static final int SKETCH_BITS_PER_QUANT = 32;
  private final HashFunction<Vec>[] hashes;
  private final List<TIntArrayList> sketches = new ArrayList<>();
  private final TLongArrayList ids = new TLongArrayList();
  private final List<Vec> vecs = new ArrayList<>();

  private final int dim;
  private int minDist;

  public QuantLSHCosIndex(FastRandom rng, int quantDim, int dim, int minDist) {
    this.dim = dim;
    this.minDist = minDist;

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

  public int[] sketch(Vec x) {
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

  @Override
  public Stream<Entry> nearest(Vec query) {
    final double queryNorm = VecTools.norm(query);
    final int[] sketch = sketch(query);

    return IntStream.range(minDist, hashes.length + 1).mapToObj(diff ->
        resultsWithDiff(sketch, diff, diff == minDist)
            .mapToObj(idx -> {
              final Vec vec = vecs.get(idx);
              return new EntryImpl(ids.getQuick(idx), vec, (1 - VecTools.multiply(query, vec) / queryNorm));
            })
            .sorted(EntryImpl::compareTo)
            .map(Functions.cast(Entry.class))
    ).flatMap(Function.identity());
  }

  @Override
  public synchronized void append(long id, Vec vec) {
    final int[] sketch = sketch(vec);
    vecs.add(vec);
    ids.add(id);
    for (int i = 0; i < sketch.length; i++) {
      sketches.get(i).add(sketch[i]);
    }
  }

  @Override
  public synchronized void remove(long id) {
    final int index = ids.indexOf(id);
    ids.remove(index, 1);
    for (int i = 0; i < sketches.size(); i++) {
      sketches.get(i).remove(index, 1);
    }
  }
}
