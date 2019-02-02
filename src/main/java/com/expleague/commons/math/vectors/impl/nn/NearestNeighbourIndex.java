package com.expleague.commons.math.vectors.impl.nn;

import com.expleague.commons.math.vectors.Distance;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.nn.lsh.LSHCosIndex;
import com.expleague.commons.random.FastRandom;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface NearestNeighbourIndex {
  int dim();

  Stream<Entry> nearest(Vec query);

  void append(long id, Vec vec);
  void remove(long id);

  static NearestNeighbourIndex create(Distance type, int dim) {
    switch (type) {
      case COS:
        return new LSHCosIndex(new FastRandom(), 4, 20, dim);
      case L2:
        return null;
    }
    throw new IllegalArgumentException();
  }

  class Entry implements Comparable<Entry> {
    private final long id;
    private final Vec vec;
    private final double distance;

    public Entry(long id, Vec vec, double distance) {
      this.id = id;
      this.vec = vec;
      this.distance = distance;
    }

    public long id() {
      return id;
    }

    public Vec vec() {
      return vec;
    }

    @Override
    public String toString() {
      return "Entry{" +
          "id=" + id +
          ", vec=" + vec +
          ", distance=" + distance +
          '}';
    }

    @Override
    public int compareTo(@NotNull Entry o) {
      return Double.compare(distance, o.distance);
    }

    public double distance() {
      return distance;
    }
  }
}
