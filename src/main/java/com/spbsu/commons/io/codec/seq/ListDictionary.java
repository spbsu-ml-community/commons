package com.spbsu.commons.io.codec.seq;

import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.seq.IntSeq;
import com.spbsu.commons.seq.IntSeqBuilder;
import com.spbsu.commons.seq.Seq;
import com.spbsu.commons.util.Pair;

import java.util.*;

/**
 * User: solar
 * Date: 22.05.14
 * Time: 16:18
 */
public class ListDictionary<T extends Comparable<T>> implements Dictionary<T> {
  public static final String DICTIONARY_INDEX_IS_CORRUPTED = "Dictionary index is corrupted!";
  private final Seq<T>[] sex;
  private final int[] parents;
  private final Comparator<Seq<T>> cmp;

  @SafeVarargs
  public ListDictionary(final Seq<T>... sex) {
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
      initalDict.add((Seq<T>)CharSeqTools.create(new Object[]{character}));
    }
    //noinspection unchecked
    return initalDict.toArray(new Seq[initalDict.size()]);
  }

  @Override
  public int search(final Seq<T> seq) {
    int index = Arrays.binarySearch(sex, seq, cmp);
    if (index >= 0)
      return index;
    index = -index-2;
    while (index >= 0) {
      if (CharSeqTools.startsWith(seq, sex[index]))
        return index;
      index = parents[index];
    }
    throw new RuntimeException(DICTIONARY_INDEX_IS_CORRUPTED);
  }

  public IntSeq parse(Seq<T> seq) {
    final IntSeqBuilder builder = new IntSeqBuilder();
    Seq<T> suffix = seq;
    while (suffix.length() > 0) {
      int symbol;
      try {
        symbol = search(suffix);
        suffix = suffix.sub(get(symbol).length(), suffix.length());
      }
      catch (RuntimeException e) {
        if (DICTIONARY_INDEX_IS_CORRUPTED.equals(e.getMessage())) {
          symbol = -1;
          suffix = suffix.sub(1, suffix.length());
        }
        else throw e;
      }
      builder.add(symbol);
    }
    return builder.build();
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
