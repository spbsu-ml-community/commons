package com.expleague.commons.math.vectors.impl.nn.faiss;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.VecTools;
import com.expleague.commons.math.vectors.impl.nn.NearestNeighbourIndex.Entry;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Faiss {
    private final int dim;
    private final int partDim;
    private final int partsNum;
    private final int batchSize;
    private final int areaCentroidsNum;
    private final int partCentroidsNum;
    private final AreaIndex[] areaIndices;

    private Vec[] areaCentroids;
    private TLongArrayList ids = new TLongArrayList();
    private List<Vec> vecs = new ArrayList<>();

    public Faiss(int dim, int partDim, int batchSize, int areaCentroidsNum, int partCentroidsNum) {
        this.dim = dim;
        this.partDim = partDim;
        partsNum = dim / partDim;
        this.batchSize = batchSize;
        this.areaCentroidsNum = areaCentroidsNum;
        this.partCentroidsNum = partCentroidsNum;
        areaIndices = new AreaIndex[areaCentroidsNum];
    }

    public void build() {
        areaCentroids = getCentroids(
                areaCentroidsNum,
                i -> vecs.get(i % vecs.size())
        );
        TIntList[] areas = new TIntList[areaCentroidsNum];
        for (int i = 0; i < areas.length; i++) {
            areas[i] = new TIntArrayList();
        }
        for (int i = 0; i < vecs.size(); i++) {
            int areaCentroidI = nearestCentroid(vecs.get(i), areaCentroids);
            areas[areaCentroidI].add(i);
        }

        for (int areaCentroidI = 0; areaCentroidI < areaCentroidsNum; areaCentroidI++) {
            Vec areaCentroid = areaCentroids[areaCentroidI];
            TIntList area = areas[areaCentroidI];
            System.out.println("areaCentroidI: " + areaCentroidI + ", areaSize: " + area.size());
            if (!area.isEmpty()) {
                Vec[][] partCentroids = new Vec[partsNum][partCentroidsNum];
                for (int partNum = 0; partNum < partsNum; partNum++) {
                    Vec[] partVecs = new Vec[area.size()];
                    for (int i = 0; i < partVecs.length; i++) {
                        partVecs[i] = VecTools.subtract(
                                vecs.get(area.get(i)),
                                areaCentroid
                        ).sub(partDim * partNum, partDim);
                    }
                    partCentroids[partNum] = getCentroids(
                            partCentroidsNum,
                            i -> partVecs[i % partVecs.length]
                    );
                }

                areaIndices[areaCentroidI] = new AreaIndex(dim, partDim, batchSize, partCentroids);
                final int fAreaCentroidI = areaCentroidI;
                area.forEach(index -> {
                    areaIndices[fAreaCentroidI].append(ids.get(index), vecs.get(index));
                    return true;
                });
            }
        }
        ids = null;
        vecs = null;
    }

    public Stream<Entry> nearest(Vec qVec) {
        int areaCentroidI = nearestCentroid(qVec, areaCentroids);
        if (areaIndices[areaCentroidI] == null) {
            return Stream.empty();
        }
        return areaIndices[areaCentroidI].nearest(qVec);
    }

    private Vec[] getCentroids(int num, IntFunction<Vec> getVec) {
        return VecTools.kMeans(
                num,
                IntStream.range(0, Integer.MAX_VALUE).mapToObj(getVec)
        );
    }

    static int nearestCentroid(Vec vec, Vec[] cntrds) {
        double minDist = VecTools.distance(vec, cntrds[0]);
        int minIndex = 0;
        for (int i = 1; i < cntrds.length; i++) {
            Vec cntrd = cntrds[i];
            double dist = VecTools.distance(cntrd, vec);
            if (dist < minDist) {
                minDist = dist;
                minIndex = i;
            }
        }
        return minIndex;
    }

    public void append(long id, Vec vec) {
        ids.add(id);
        vecs.add(vec);
    }
}
