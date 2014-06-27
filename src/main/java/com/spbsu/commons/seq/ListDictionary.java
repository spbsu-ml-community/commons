package com.spbsu.commons.seq;

import com.spbsu.commons.math.vectors.Vec;
import com.spbsu.commons.random.FastRandom;
import com.spbsu.commons.text.CharArrayCharSequence;
import com.spbsu.commons.text.CharSequenceTools;
import com.spbsu.commons.util.Pair;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

/**
* User: solar
* Date: 22.05.14
* Time: 16:18
*/
public class ListDictionary {
  private CharSequence[] sex;
  private int[] parents;
  private CharSequenceTools.LexicographicalComparator cmp = new CharSequenceTools.LexicographicalComparator();

  public ListDictionary(CharSequence... sex) {
    this.sex = sex;
    this.parents = new int[sex.length];
    Stack<Pair<CharSequence,Integer>> parents = new Stack<Pair<CharSequence, Integer>>();
    Arrays.sort(this.sex, cmp);

    for (int i = 0; i < sex.length; i++) {
      CharSequence current = this.sex[i];
      this.parents[i] = -1;
      while (!parents.empty()) {
        if(CharSequenceTools.startsWith(current, parents.peek().getFirst())) {
          this.parents[i] = parents.peek().getSecond();
          break;
        }
        parents.pop();
      }
      parents.push(Pair.create(current, i));
    }
  }

  public ListDictionary(Character... chars) {
    this(convertToSeqs(chars));
  }

  private static CharSequence[] convertToSeqs(Character[] chars) {
    final List<CharSequence> initalDict = new ArrayList<CharSequence>(chars.length);
    for (Character character : chars) {
      char[] s = new char[]{character};
      initalDict.add(new CharArrayCharSequence(s, 0, 1));
    }
    return initalDict.toArray(new CharSequence[initalDict.size()]);
  }

  public int search(CharSequence seq) {
    int index = Arrays.binarySearch(sex, seq, cmp);
    if (index >= 0)
      return index;
    index = -index-2;
    do {
      if (CharSequenceTools.startsWith(seq, sex[index]))
        return index;
      index = parents[index];
    }
    while(index >= 0);
    throw new RuntimeException("Dictionary index is corrupted!");
  }

  public int encode(CharSequence seq, TIntArrayList result) {
    int count = 0;
    while(seq.length() > 0) {
      final int symbol = search(seq);
      result.add(symbol);
      count++;
      seq = seq.subSequence(get(symbol).length(), seq.length());
    }
    return count;
  }

  public CharSequence get(int index) {
    return sex[index];
  }

  public int size() {
    return sex.length;
  }

  public Collection<? extends CharSequence> alphabet() {
    return Arrays.asList(sex);
  }

  public int parent(int second) {
    return parents[second];
  }

  public CharSequence next(Vec probabs, FastRandom rng) {
    return get(rng.nextSimple(probabs));
  }
}
