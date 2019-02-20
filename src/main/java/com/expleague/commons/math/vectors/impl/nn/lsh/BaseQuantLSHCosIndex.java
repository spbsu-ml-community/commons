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

public abstract class BaseQuantLSHCosIndex implements NearestNeighbourIndex {
    public static final int SKETCH_BITS_PER_QUANT = 32;
    protected final CosDistanceHashFunction[] hashes;
    protected final List<BitSet> sketches;
    protected final TLongArrayList ids;

    protected int quantDim;
    protected final int dim;
    protected final int batchSize;

    public BaseQuantLSHCosIndex(FastRandom rng, int quantDim, int dim, int batchSize) {
        this.quantDim = quantDim;
        this.dim = dim;
        this.batchSize = batchSize;
        this.ids = new TLongArrayList();
        this.sketches = new ArrayList<>();

        //noinspection unchecked
        this.hashes = new CosDistanceHashFunction[SKETCH_BITS_PER_QUANT * (int)Math.ceil(dim / (double)quantDim)];
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
        }
    }

    public BaseQuantLSHCosIndex(int dim, int batchSize, TLongArrayList ids, List<TIntArrayList> sketches, CosDistanceHashFunction[] hashes) {
        this.dim = dim;
        this.batchSize = batchSize;
        this.ids = ids;
        this.hashes = hashes;
        this.sketches = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            byte[] sketchBytes = new byte[sketches.size() * 4];
            for (int s = 0; s < sketches.size(); s++) {
                int s_s = sketches.get(s).getQuick(i);
                for (int k = 0; k < 4; k++, s_s >>>= 8) {
                    sketchBytes[s * 4 + k] = (byte)(0xFF & s_s);
                }
            }
            this.sketches.add(BitSet.valueOf(sketchBytes));
        }
    }

    @Override
    public int dim() {
        return dim;
    }

    private Stream<EntryImpl> orderBySketch(BitSet sketch) {
        return IntStream.range(0, ids.size())
//            .parallel()
            .mapToObj(i -> {
                sketch.xor(sketches.get(i));
                int xorBits = sketch.cardinality();
                sketch.xor(sketches.get(i));
                return new EntryImpl(i,
                    ids.getQuick(i),
                    xorBits);
            })
            .sorted(Comparator.comparingInt(EntryImpl::hashDistance));
    }

    private BitSet sketch(Vec x) {
        final BitSet result = new BitSet(hashes.length);
        int bit = 1;
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
                    entry.setVec(vec);
                    entry.setDistance((1 - VecTools.multiply(query, vec) / queryNorm) / 2);
                })
                .sorted(EntryImpl::compareTo)
            )
            .map(Functions.cast(Entry.class));
    }

    protected synchronized void baseAppend(long id, Vec vec) {
        sketches.add(sketch(vec));
        ids.add(id);
    }

    protected synchronized int baseRemove(long id) {
        final int index = ids.indexOf(id);
        ids.remove(index, 1);
        sketches.remove(index);
        return index;
    }
}
