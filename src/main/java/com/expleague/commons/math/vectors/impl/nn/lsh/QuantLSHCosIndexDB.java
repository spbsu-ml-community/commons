package com.expleague.commons.math.vectors.impl.nn.lsh;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.commons.random.FastRandom;
import com.google.common.primitives.Longs;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.file.Path;
import java.util.stream.Stream;

public class QuantLSHCosIndexDB extends BaseQuantLSHCosIndex {
    private static final long DEFAULT_CACHE_SIZE = 1 << 10;
    private static final Options DB_OPTIONS = new Options()
            .cacheSize(DEFAULT_CACHE_SIZE)
            .createIfMissing(true);
    private final DB vecDB;

    public QuantLSHCosIndexDB(FastRandom rng, int quantDim, int dim, int minDist, Path dbPath) throws IOException {
        super(rng, quantDim, dim, minDist);
        vecDB = JniDBFactory.factory.open(dbPath.toFile(), DB_OPTIONS);
    }

    @Override
    public Stream<Entry> nearest(Vec query) {
        return baseNearest(query, idx -> {
            long id = ids.getQuick(idx);
            byte[] bytes = vecDB.get(Longs.toByteArray(id));
            DoubleBuffer doubleBuf = ByteBuffer.wrap(bytes).asDoubleBuffer();
            double[] doubles = new double[doubleBuf.remaining()];
            doubleBuf.get(doubles);
            return new ArrayVec(doubles);
        });
    }

    @Override
    public synchronized void append(long id, Vec vec) {
        baseAppend(id, vec);
        double[] coords = vec.toArray();
        ByteBuffer byteBuf = ByteBuffer.allocate(Double.BYTES * coords.length);
        DoubleBuffer doubleBuf = byteBuf.asDoubleBuffer();
        doubleBuf.put(coords);
        vecDB.put(Longs.toByteArray(id), byteBuf.array());
    }

    @Override
    public synchronized void remove(long id) {
        baseRemove(id);
        vecDB.delete(Longs.toByteArray(id));
    }
}
