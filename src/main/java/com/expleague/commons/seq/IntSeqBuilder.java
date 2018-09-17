package com.expleague.commons.seq;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * User: qdeee
 * Date: 08.09.14
 */
public class IntSeqBuilder implements SeqBuilder<Integer> {
  private final TIntList data;

  public IntSeqBuilder() {
    data = new TIntArrayList();
  }

  public IntSeqBuilder append(final int value) {
    data.add(value);
    return this;
  }

  public IntSeqBuilder append(final int... value) {
    data.add(value);
    return this;
  }

  @Override
  public IntSeqBuilder add(final Integer integer) {
    append(integer);
    return this;
  }

  @Override
  public IntSeqBuilder addAll(final Seq<Integer> values) {
    for (int i = 0; i < values.length(); i++) {
      append(values.at(i));
    }
    return this;
  }

  public IntSeqBuilder addAll(final IntSeqBuilder other) {
    data.addAll(other.data);
    return this;
  }

  @Override
  public IntSeq build() {
    final int mark = mark();
    final int[] resultArr = new int[data.size() - mark];
    data.toArray(resultArr, mark, resultArr.length);
    reset();
    return new IntSeq(resultArr);
  }

  public IntSeq buildAll() {
    final int[] resultArr = new int[data.size()];
    data.toArray(resultArr, 0, resultArr.length);
    return new IntSeq(resultArr);
  }

  @Override
  public void clear() {
    data.clear();
  }

  private int mark() {
    if (marks == null || marks.isEmpty())
      return 0;
    return marks.get(marks.size() - 1);
  }

  public int length() {
    return data.size();
  }


  private TIntList marks;
  public void pushMark() {
    if (marks == null)
      marks = new TIntArrayList();
    marks.add(length());
  }

  public void popMark() {
    marks.removeAt(marks.size() - 1);
  }

  public void reset() {
    if (marks != null && !marks.isEmpty()) {
      final int mark = marks.get(marks.size() - 1);
      data.remove(mark, data.size() - mark);
    }
    else data.clear();
  }
}
