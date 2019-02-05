package com.expleague.commons.math.vectors.impl.nn;

import com.expleague.commons.math.vectors.Distance;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.nn.lsh.LSHCosIndex;
import com.expleague.commons.random.FastRandom;

import java.util.stream.Stream;

public interface NearestNeighbourIndex {
  int dim();

  Stream<Entry> nearest(Vec query);

  void append(long id, Vec vec);
  void remove(long id);

  interface Entry extends Comparable<Entry> {
    long id();
    Vec vec();
    double distance();
  }

  static NearestNeighbourIndex create(Distance type, int dim) {
    switch (type) {
      case COS:
        return new LSHCosIndex(new FastRandom(), 24, dim);
      case L2:
        return null;
    }
    throw new IllegalArgumentException();
  }
}
