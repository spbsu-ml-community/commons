package com.spbsu.commons.io.codec.seq;

import java.util.*;


import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.seq.Seq;
import com.spbsu.commons.util.Pair;
import gnu.trove.list.array.TIntArrayList;

/**
* User: solar
* Date: 22.05.14
* Time: 16:18
*/
public class ListDictionary<T extends Comparable<T>> {
  private final Seq<T>[] sex;
  private final int[] parents;
  private final Comparator<Seq<T>> cmp;

  @SafeVarargs
  public ListDictionary(Seq<T>... sex) {
    this.sex = sex;
    this.parents = new int[sex.length];
    Stack<Pair<Seq<T>,Integer>> parents = new Stack<>();
    cmp = CharSeqTools.lexicographicalComparator(sex[0].elementType());
    Arrays.sort(this.sex, cmp);

    for (int i = 0; i < sex.length; i++) {
      Seq<T> current = this.sex[i];
      this.parents[i] = -1;
      while (!parents.empty()) {
        if(CharSeqTools.startsWith(current, parents.peek().getFirst())) {
          this.parents[i] = parents.peek().getSecond();
          break;
        }
        parents.pop();
      }
      parents.push(Pair.create(current, i));
    }
  }

  @SafeVarargs
  public ListDictionary(T... chars) {
    this(convertToSeqs(chars));
  }

  private static <T> Seq<T>[] convertToSeqs(T[] chars) {
    final List<Seq<T>> initalDict = new ArrayList<>(chars.length);
    for (T character : chars) {
      //noinspection unchecked
      initalDict.add(CharSeqTools.<T>create(character));
    }
    //noinspection unchecked
    return initalDict.toArray(new Seq[initalDict.size()]);
  }

  public int search(Seq<T> seq) {
    int index = Arrays.binarySearch(sex, seq, cmp);
    if (index >= 0)
      return index;
    index = -index-2;
    do {
      if (CharSeqTools.startsWith(seq, sex[index]))
        return index;
      index = parents[index];
    }
    while(index >= 0);
    throw new RuntimeException("Dictionary index is corrupted!");
  }

  public int encode(Seq<T> seq, TIntArrayList result) {
    int count = 0;
    while(seq.length() > 0) {
      final int symbol = search(seq);
      result.add(symbol);
      count++;
      seq = seq.sub(get(symbol).length(), seq.length());
    }
    return count;
  }

  public Seq<T> get(int index) {
    return sex[index];
  }

  public int size() {
    return sex.length;
  }

  public Collection<? extends Seq<T>> alphabet() {
    return Arrays.asList(sex);
  }

  public int parent(int second) {
    return parents[second];
  }

  public Seq<T> next(Vec probabs, FastRandom rng) {
    return get(rng.nextSimple(probabs));
  }
}
