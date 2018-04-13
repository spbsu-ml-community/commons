package com.expleague.commons.io.codec.seq;

import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.IntSeq;
import com.expleague.commons.seq.Seq;
import com.expleague.commons.seq.regexp.Alphabet;
import com.expleague.commons.seq.regexp.Matcher;
import gnu.trove.list.TIntList;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.set.TIntSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User: solar
 * Date: 30.09.15
 * Time: 18:39
 */
public interface Dictionary<T extends Comparable<T>> extends Alphabet<Seq<T>>{
  int search(Seq<T> seq);
  int search(Seq<T> seq, TIntSet excludes);

  IntSeq parse(Seq<T> seq, TIntList freqs, double totalFreq);
  IntSeq parse(Seq<T> seq);
  IntSeq parse(Seq<T> seq, TIntSet excludes);
  void visitVariants(Seq<T> arg, TIntList freqs, double totalFreq, TObjectDoubleProcedure<IntSeq> todo);

  Seq<T> get(int index);

  int size();

  List<? extends Seq<T>> alphabet();

  int parent(int second);

  Dictionary EMPTY = new Dictionary() {
    @Override
    public int search(Seq seq) {
      return -1;
    }

    @Override
    public int search(Seq seq, TIntSet excludes) {
      return -1;
    }

    @Override
    public IntSeq parse(Seq seq, TIntList freqs, double totalFreq) {
      return parse(seq);
    }

    @Override
    public IntSeq parse(Seq seq) {
      final int[] arr = new int[seq.length()];
      Arrays.fill(arr, -1);
      return new IntSeq(arr);
    }

    @Override
    public IntSeq parse(Seq seq, TIntSet excludes) {
      return parse(seq);
    }

    @Override
    public void visitVariants(Seq arg, TIntList freqs, double totalFreq, TObjectDoubleProcedure todo) {
      todo.execute(parse(arg, freqs, totalFreq), 1);
    }

    @Override
    public Seq get(int index) {
      return null;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public List<? extends Seq> alphabet() {
      return Collections.emptyList();
    }

    @Override
    public int parent(int second) {
      return -1;
    }

    @Override
    public int index(Object ts) {
      return -1;
    }

    @Override
    public SeqCondition conditionByT(Object i) {
      return null;
    }
  };


  @Override
  default int indexCondition(Matcher.Condition<Seq<T>> c) {
    return search(((SeqCondition<T>) c).getSeq());
  }

  @Override
  default SeqCondition<T> condition(int i) {
    return new SeqCondition<>(get(i));
  }

  @Override
  default SeqCondition<T> conditionByT(Seq<T> i) {
    return new SeqCondition<>(i);
  }

  @Override
  default Seq<T> getT(Matcher.Condition<Seq<T>> condition) {
    return ((SeqCondition<T>) condition).getSeq();
  }

  @Override
  default int index(Seq<T> ts) {
    return search(ts);
  }
}
