package com.expleague.commons.seq;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;

/**
 * User: qdeee
 * Date: 08.09.14
 */
public class LongSeqBuilder implements SeqBuilder<Long> {
  private final TLongArrayList data;

  public LongSeqBuilder() {
    data = new TLongArrayList();
  }
  public LongSeqBuilder(int capacity) {
    data = new TLongArrayList(capacity);
  }

  public LongSeqBuilder append(final long value) {
    data.add(value);
    return this;
  }

  public LongSeqBuilder append(final long... value) {
    data.add(value);
    return this;
  }

  public LongSeqBuilder append(final long[] value, int start, int length) {
    data.add(value, start, length);
    return this;
  }

  @Override
  public LongSeqBuilder add(final Long integer) {
    append(integer);
    return this;
  }

  @Override
  public LongSeqBuilder addAll(final Seq<Long> values) {
    if (values instanceof LongSeq) {
      final LongSeq valuesSeq = (LongSeq) values;
      append(valuesSeq.arr, valuesSeq.start, values.length());
    }
    else {
      for (int i = 0; i < values.length(); i++) {
        append(values.at(i));
      }
    }
    return this;
  }

  public LongSeqBuilder addAll(final LongSeqBuilder other) {
    data.addAll(other.data);
    return this;
  }

  @Override
  public LongSeq build() {
    final int mark = mark();
    final long[] resultArr = new long[data.size() - mark];
    data.toArray(resultArr, mark, resultArr.length);
    reset();
    return new LongSeq(resultArr);
  }

  public LongSeq build(long[] reuse, double extra, int minExtra) {
    final int mark = mark();
    int length = data.size() - mark;
    final long[] resultArr;
    if (reuse.length >= length)
      resultArr = reuse;
    else
      resultArr = new long[Math.max(length + minExtra, (int) Math.ceil((length) * (1 + extra)))];
    data.toArray(resultArr, mark, length);
    reset();
    return new LongSeq(resultArr, 0, length);
  }

  public LongSeq buildAll() {
    final long[] resultArr = new long[data.size()];
    data.toArray(resultArr, 0, resultArr.length);
    return new LongSeq(resultArr);
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
    else data.resetQuick();
  }
}
