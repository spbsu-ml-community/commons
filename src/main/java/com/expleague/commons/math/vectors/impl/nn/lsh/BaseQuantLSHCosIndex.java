package com.expleague.commons.math.vectors.impl.nn.lsh;

import com.expleague.commons.func.Functions;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.nn.NearestNeighbourIndex;
import com.expleague.commons.math.vectors.impl.nn.impl.EntryImpl;
import com.expleague.commons.random.FastRandom;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess")
public abstract class BaseQuantLSHCosIndex implements NearestNeighbourIndex {
  protected final CosDistanceHashFunction[] hashes;
  protected final List<TLongArrayList> sketches = new ArrayList<>();
  protected final TLongArrayList ids;

  protected int quantDim;
  protected int sketchBitsPerQuant;
  protected final int dim;
  protected final int batchSize;

  public BaseQuantLSHCosIndex(FastRandom rng, int dim, int quantDim, int sketchBitsPerQuant, int batchSize) {
    this.dim = dim;
    this.quantDim = quantDim;
    this.sketchBitsPerQuant = sketchBitsPerQuant;
    this.batchSize = batchSize;
    this.ids = new TLongArrayList();

    this.hashes = new CosDistanceHashFunction[sketchBitsPerQuant * (int)Math.ceil(dim / (double)quantDim)];
    for (int i = 0; i < dim; i += quantDim) {
      int finalI = i;
      final int currentDim = Math.min(quantDim, dim - i);
      for (int b = 0; b < sketchBitsPerQuant; b++) {
        hashes[i / quantDim * sketchBitsPerQuant + b] = new CosDistanceHashFunction(currentDim, rng) { // quant sketch
          @Override
          public int hash(Vec v) {
            return super.hash(v.sub(finalI, currentDim));
          }
        };
      }
    }
    for (int i = 0; i < (int)Math.ceil(hashes.length / 64.); i++) {
      sketches.add(new TLongArrayList());
    }
  }

  public BaseQuantLSHCosIndex(int dim, int batchSize, TLongArrayList ids, List<TLongArrayList> sketches, CosDistanceHashFunction[] hashes) {
    this.dim = dim;
    this.batchSize = batchSize;
    this.ids = ids;
    this.sketches.addAll(sketches);
    this.hashes = hashes;
  }

  @Override
  public int dim() {
    return dim;
  }

  private Stream<EntryImpl> orderBySketch(BitSet sketch) {
    int[] distance = new int[ids.size()];
    final long[] sketchL = sketch.toLongArray();
    final TIntArrayList[] buckets = new TIntArrayList[hashes.length];

    for (int i = 0; i < sketches.size(); i++) {
      final long qsketch = sketchL[i];
      final TLongArrayList sketches_i = sketches.get(i);
      IntStream.range(0, sketches_i.size()).parallel().forEach(j -> {
        distance[j] += Long.bitCount(sketches_i.getQuick(j) ^ qsketch);
      });
    }

    for (int i = 0; i < distance.length; i++) {
      final int dist = distance[i];
      TIntArrayList bucket = buckets[dist];
      if (bucket == null)
        bucket = buckets[dist] = new TIntArrayList(1000);
      bucket.add(i);
    }

//    ArrayTools.parallelSort(distance, order);
    return IntStream.range(0, buckets.length)
        .mapToObj(d -> buckets[d] == null ?
            Stream.<EntryImpl>empty() :
            IntStream.of(buckets[d].toArray()).mapToObj(i -> new EntryImpl(i, ids.getQuick(i), d))
        )
        .flatMap(Function.identity());
  }

  protected BitSet sketch(Vec x) {
    final BitSet result = new BitSet(hashes.length);
    for (int h = 0; h < hashes.length; h++) {
      if (hashes[h].hash(x) > 0)
        result.set(h);
    }
    return result;
  }

  protected Stream<Entry> baseNearest(Vec query, IntFunction<Vec> getVec) {
    final double queryNorm = VecTools.norm(query);
    final BitSet sketch = sketch(query);

    return Stream.concat(orderBySketch(sketch), Stream.of((EntryImpl)null))
        .map(new Function<EntryImpl, List<EntryImpl>>() {
          List<EntryImpl> batch = new ArrayList<>(batchSize);
          @Override
          public List<EntryImpl> apply(EntryImpl entry) {
            if (entry == null)
              return batch;
            batch.add(entry);
            if (batch.size() == batchSize) {
              final List<EntryImpl> copy = this.batch;
              this.batch = new ArrayList<>(batchSize);
              return copy;
            }
            return null;
          }
        })
        .filter(Objects::nonNull)
        .flatMap(entries -> entries.stream()
            .peek(entry -> {
              final Vec vec = getVec.apply(entry.index());
              if (vec == null)
                return;
              entry.setVec(vec);
              entry.setDistance((1 - VecTools.multiply(query, vec) / queryNorm) / 2);
            })
            .filter(entry -> entry.vec() != null)
            .sorted(EntryImpl::compareTo)
        )
        .map(Functions.cast(Entry.class));
  }

  protected synchronized void baseAppend(long id, Vec vec) {
    final long[] sketch = sketch(vec).toLongArray();
    ids.add(id);
    for (int i = 0; i < sketch.length; i++) {
      sketches.get(i).add(sketch[i]);
    }
  }

  protected synchronized int baseRemove(long id) {
    final int index = ids.indexOf(id);
    ids.remove(index, 1);
    for (TLongArrayList sketch : sketches) {
      sketch.remove(index, 1);
    }
    return index;
  }
}
