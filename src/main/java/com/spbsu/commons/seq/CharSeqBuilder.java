package com.spbsu.commons.seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("EqualsAndHashcode")
public class CharSeqBuilder extends CharSeqComposite {
  private final List<CharSequence> fragments;

  public CharSeqBuilder(CharSequence... fragments) {
    this(Arrays.asList(fragments));
  }

  public CharSeqBuilder(List<CharSequence> fragments) {
    this.fragments = CharSeqTools.discloseComposites(fragments);
  }

  public CharSeqBuilder(final int parts) {
    this.fragments = new ArrayList<CharSequence>(parts);
  }

  public CharSeqBuilder append(CharSequence next) {
    if (CharSeqTools.isImmutable(next))
      add(next);
    else if (next instanceof CharSeqComposite) {
      final CharSeqComposite composite = (CharSeqComposite) next;
      for (int i = 0; i < composite.fragmentsCount(); i++) {
        append(composite.fragment(i));
      }
    }
    else add(copy(next));
    return this;
  }

  public CharSeqBuilder append(char ch) {
    add(new CharSeqChar(ch));
    return this;
  }

  public CharSeqBuilder append(int n) {
    add(Integer.toString(n));
    return this;
  }

  public CharSeqBuilder append(float n) {
    add(Float.toString(n));
    return this;
  }

  public CharSeqBuilder append(double n) {
    add(Double.toString(n));
    return this;
  }

  public CharSeqBuilder append(boolean b) {
    add(Boolean.toString(b));
    return this;
  }

  public CharSeqBuilder append(Object o) {
    add(String.valueOf(o));
    return this;
  }

  public CharSeqBuilder append(char[] text) {
    add(copy(text));
    return this;
  }

  public CharSeqBuilder append(char[] text, int start, int end) {
    add(copy(text, start, end));
    return this;
  }

  protected void add(final CharSequence copy) {
    fragments.add(copy);
    hashCode = 0;
  }

  public final int fragmentsCount() {
    return fragments.size();
  }

  @Override
  public boolean isImmutable() {
    return false;
  }

  @Override
  public final CharSequence fragment(final int j) {
    return fragments.get(j);
  }
}
