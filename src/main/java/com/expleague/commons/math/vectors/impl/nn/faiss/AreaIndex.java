package com.expleague.commons.math.vectors.impl.nn.faiss;

import com.expleague.commons.func.Functions;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.nn.NearestNeighbourIndex.Entry;
import com.expleague.commons.math.vectors.impl.nn.impl.EntryImpl;
import com.expleague.commons.util.ArrayTools;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AreaIndex {
    private final int dim;
    private final int partDim;
    private final int partsNum;
    private final int batchSize;
    private final Vec[][] partCentroids;
    private final TLongArrayList ids = new TLongArrayList();
    private final List<Vec> vecs = new ArrayList<>();
    private final List<int[]> sketches = new ArrayList<>();

    AreaIndex(int dim, int partDim, int batchSize, Vec[][] partCentroids) {
        this.dim = dim;
        this.partDim = partDim;
        this.partsNum = dim / partDim;
        this.batchSize = batchSize;
        this.partCentroids = partCentroids;
    }

    private int dist(int[] sketch1, int[] sketch2) {
        int cnt = 0;
        for (int i = 0; i < sketch1.length; i++) {
            if (sketch1[i] != sketch2[i]) {
                cnt++;
            }
        }
        return cnt;
    }

    private Stream<EntryImpl> orderBySketch(int[] sketch) {
        int[] order = ArrayTools.sequence(0, ids.size());
        int[] distance = new int[ids.size()];

        IntStream.range(0, sketches.size()).parallel().forEach(i -> distance[i] = dist(sketches.get(i), sketch));

        ArrayTools.parallelSort(distance, order);
        return IntStream.of(order).mapToObj(i -> new EntryImpl(i, ids.getQuick(i), distance[i]));
    }

    private int[] sketch(Vec vec) {
        int[] sketch = new int[partsNum];
        for (int partNum = 0; partNum < partsNum; partNum++) {
            sketch[partNum] = Faiss.nearestCentroid(vec.sub(partDim * partNum, partDim), partCentroids[partNum]);
        }
        return sketch;
    }

    //Copypaste from BaseQuantLSHCosIndex
    public Stream<Entry> nearest(Vec qVec) {
        int[] sketch = sketch(qVec);

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
                            final Vec vec = vecs.get(entry.index());
                            if (vec == null)
                                return;
                            entry.setVec(vec);
                            entry.setDistance(VecTools.distance(qVec, vec));
                        })
                        .filter(entry -> entry.vec() != null)
                        .sorted(EntryImpl::compareTo)
                )
                .map(Functions.cast(Entry.class));
    }

    public void append(long id, Vec vec) {
        ids.add(id);
        vecs.add(vec);
        sketches.add(sketch(vec));
    }
}
