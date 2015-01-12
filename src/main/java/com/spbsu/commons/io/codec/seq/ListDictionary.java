package com.spbsu.commons.io.codec.seq;

import java.util.*;


import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.seq.Seq;
import com.spbsu.commons.util.Pair;

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
  public ListDictionary(final Seq<T>... sex) {
    this.sex = sex;
    this.parents = new int[sex.length];
    final Stack<Pair<Seq<T>,Integer>> parents = new Stack<>();
    cmp = CharSeqTools.lexicographicalComparator(sex[0].elementType());
    Arrays.sort(this.sex, cmp);

    for (int i = 0; i < sex.length; i++) {
      final Seq<T> current = this.sex[i];
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
  public ListDictionary(final T... chars) {
    this(convertToSeqs(chars));
  }

  private static <T> Seq<T>[] convertToSeqs(final T[] chars) {
    final List<Seq<T>> initalDict = new ArrayList<>(chars.length);
    for (final T character : chars) {
      //noinspection unchecked
      initalDict.add((Seq<T>)CharSeqTools.create(new Object[]{character}));
    }
    //noinspection unchecked
    return initalDict.toArray(new Seq[initalDict.size()]);
  }

  public int search(final Seq<T> seq) {
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

  public Seq<T> get(final int index) {
    return sex[index];
  }

  public int size() {
    return sex.length;
  }

  public Collection<? extends Seq<T>> alphabet() {
    return Arrays.asList(sex);
  }

  public int parent(final int second) {
    return parents[second];
  }
}
