package com.spbsu.commons.seq;

import java.util.Arrays;
import java.util.List;

public class CharSeqBuilder extends CharSeqComposite {
  private final List<CharSequence> fragments;

  public CharSeqBuilder(CharSequence... fragments) {
    this(Arrays.asList(fragments));
  }

  public CharSeqBuilder(List<CharSequence> fragments) {
    this.fragments = compact(fragments);
  }

  public CharSeqBuilder append(CharSequence next) {
    fragments.add(copy(next));
    return this;
  }

  public CharSeqBuilder append(char ch) {
    fragments.add(new CharSeqChar(ch));
    return this;
  }

  public CharSeqBuilder append(int n) {
    fragments.add(Integer.toString(n));
    return this;
  }

  public CharSeqBuilder append(float n) {
    fragments.add(Float.toString(n));
    return this;
  }

  public CharSeqBuilder append(char[] text) {
    fragments.add(copy(text));
    return this;
  }

  public CharSeqBuilder append(char[] text, int start, int end) {
    fragments.add(copy(text, start, end));
    return this;
  }

  public final int fragmentsCount() {
    return fragments.size();
  }

  @Override
  public final CharSequence fragment(final int j) {
    return fragments.get(j);
  }
}
