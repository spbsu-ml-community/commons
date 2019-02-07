package com.expleague.commons.math.vectors.impl.nn.lsh;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.nn.NearestNeighbourIndex;
import com.expleague.commons.random.FastRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class QuantLSHCosIndexRAM extends BaseQuantLSHCosIndex {
    private final List<Vec> vecs = new ArrayList<>();

    public QuantLSHCosIndexRAM(FastRandom rng, int quantDim, int dim, int minDist) {
        super(rng, quantDim, dim, minDist);
    }

    @Override
    public Stream<NearestNeighbourIndex.Entry> nearest(Vec query) {
        return baseNearest(query, vecs::get);
    }

    @Override
    public void append(long id, Vec vec) {
        baseAppend(id, vec);
        vecs.add(vec);
    }

    @Override
    public synchronized void remove(long id) {
        final int index = baseRemove(id);
        vecs.remove(index);
    }
}
