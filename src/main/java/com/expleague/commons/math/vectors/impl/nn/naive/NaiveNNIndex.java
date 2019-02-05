package com.expleague.commons.math.vectors.impl.nn.naive;

import com.expleague.commons.math.vectors.Distance;
import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.nn.NearestNeighbourIndex;
import com.expleague.commons.math.vectors.impl.nn.impl.EntryImpl;
import com.expleague.commons.util.ArrayTools;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NaiveNNIndex implements NearestNeighbourIndex {
  private final Distance distance;
  private final int dim;
  private TLongArrayList ids = new TLongArrayList();
  private List<Vec> vecs = new ArrayList<>();

  public NaiveNNIndex(Distance distance, int dim) {
    this.distance = distance;
    this.dim = dim;
  }

  @Override
  public int dim() {
    return dim;
  }

  @Override
  public Stream<Entry> nearest(Vec query) {
    final double[] distances = vecs.stream().mapToDouble(v -> distance.distance(query, v)).toArray();
    final int[] order = ArrayTools.sequence(0, ids.size());
    ArrayTools.parallelSort(distances, order);
    return IntStream.range(0, ids.size()).mapToObj(idx -> new EntryImpl(this.ids.get(order[idx]), this.vecs.get(order[idx]), distances[idx]));
  }

  @Override
  public void append(long id, Vec vec) {
    ids.add(id);
    vecs.add(vec);
  }

  @Override
  public void remove(long id) {
    int index = ids.indexOf(id);
    if (index < 0)
      return;
    ids.remove(index, 1);
    vecs.remove(index);
  }
}
