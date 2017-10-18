package com.expleague.commons.seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("EqualsAndHashcode")
public class CharSeqBuilder extends CharSeqComposite implements SeqBuilder<Character> {
  private final List<CharSequence> fragments;

  public CharSeqBuilder(final CharSequence... fragments) {
    this(new ArrayList<>(Arrays.asList(fragments)));
  }

  public CharSeqBuilder(final List<CharSequence> fragments) {
    this.fragments = CharSeqTools.discloseComposites(fragments);
  }

  public CharSeqBuilder(final int parts) {
    this.fragments = new ArrayList<>(parts);
  }

  public CharSeqBuilder append(final CharSequence next) {
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

  public CharSeqBuilder append(final char ch) {
    add(new CharSeqChar(ch));
    return this;
  }

  public CharSeqBuilder append(final int n) {
    add(Integer.toString(n));
    return this;
  }

  public CharSeqBuilder append(final float n) {
    add(Float.toString(n));
    return this;
  }

  public CharSeqBuilder append(final double n) {
    add(Double.toString(n));
    return this;
  }

  public CharSeqBuilder append(final boolean b) {
    add(Boolean.toString(b));
    return this;
  }

  @Override
  public CharSeqBuilder add(final Character character) {
    append(character.charValue());
    return this;
  }

  @Override
  public SeqBuilder<Character> addAll(final Seq<Character> values) {
    append(values);
    return this;
  }

  public CharSeqBuilder append(final Object o) {
    add(String.valueOf(o));
    return this;
  }

  public CharSeqBuilder append(final char[] text) {
    if (text.length > 0)
      add(copy(text));
    return this;
  }

  public CharSeqBuilder append(final char[] text, final int start, final int end) {
    if (start != end)
      add(copy(text, start, end));
    return this;
  }

  protected void add(final CharSequence copy) {
    fragments.add(copy);
    length = -1;
    hashCode = 0;
  }

  @Override
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

  public void clear() {
    fragments.clear();
    hashCode = 0;
    activeFragment = null;
    activeFragmentRangeStart = -1;
    activeFragmentRangeEnd = -1;
  }

  @Override
  public CharSeq build() {
    return new CharSeqComposite(fragments.toArray(new CharSequence[fragments.size()]));
  }
}
