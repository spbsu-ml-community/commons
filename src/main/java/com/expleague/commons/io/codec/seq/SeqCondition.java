package com.expleague.commons.io.codec.seq;

import com.expleague.commons.seq.Seq;
import com.expleague.commons.seq.regexp.Matcher;

public class SeqCondition<T> implements Matcher.Condition<Seq<T>> {
  private final Seq<T> seq;

  public SeqCondition(Seq<T> seq) {
    this.seq = seq;
  }

  public Seq<T> getSeq() {
    return seq;
  }

  @Override
  public boolean is(Seq<T> frag) {
    return seq.equals(frag);
  }

  @Override
  public String toString() {
    return seq.toString();
  }
}
