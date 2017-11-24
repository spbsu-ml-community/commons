package com.expleague.commons.io.codec.seq;

import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.seq.Seq;
import com.expleague.commons.util.Pair;
import gnu.trove.set.TIntSet;

import java.util.*;

/**
 * User: solar
 * Date: 22.05.14
 * Time: 16:18
 */
public class ListDictionary<T extends Comparable<T>> extends DictionaryBase<T> {
  public static final String DICTIONARY_INDEX_IS_CORRUPTED = "Dictionary index is corrupted!";
  private final Seq<T>[] sex;
  private final int[] parents;
  private final Comparator<Seq<T>> cmp;

  @SafeVarargs
  public ListDictionary(final Seq<T>... sex) {
    //noinspection unchecked
    this.sex = new Seq[sex.length];
    this.parents = new int[sex.length];
    final Stack<Pair<Seq<T>,Integer>> parents = new Stack<>();
    cmp = CharSeqTools.lexicographicalComparator(sex[0].elementType());
    for (int i = 0; i < sex.length; i++) {
      this.sex[i] = CharSeqTools.create(CharSeqTools.toArray(sex[i]));
    }
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

  public static <T> Seq<T>[] convertToSeqs(final T[] chars) {
    final List<Seq<T>> initalDict = new ArrayList<>(chars.length);
    for (final T character : chars) {
      //noinspection unchecked
      initalDict.add(CharSeqTools.create(new Object[]{character}));
    }
    //noinspection unchecked
    return initalDict.toArray(new Seq[initalDict.size()]);
  }

  @Override
  public int search(final Seq<T> seq, final TIntSet excludes) {
    int index = Arrays.binarySearch(sex, seq, cmp);
    if (index >= 0) {
      if (excludes == null || !excludes.contains(index))
        return index;
      index = -(parents[index] + 2);
    }
    index = -(index + 2);
    while (index >= 0) {
      if (CharSeqTools.startsWith(seq, sex[index]) && (excludes == null || !excludes.contains(index)))
        return index;
      index = parents[index];
    }
    throw new RuntimeException(DICTIONARY_INDEX_IS_CORRUPTED);
  }

  @Override
  public Seq<T> get(final int index) {
    return sex[index];
  }

  @Override
  public int size() {
    return sex.length;
  }

  @Override
  public List<? extends Seq<T>> alphabet() {
    return new ArrayList<>(Arrays.asList(sex));
  }

  @Override
  public int parent(final int second) {
    return parents[second];
  }
}
