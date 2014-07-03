package com.spbsu.commons.seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("EqualsAndHashcode")
public class CharSeqComposite extends CharSeq {
  private CharSequence[] fragments;
  private CharSequence activeFragment = null;

  private int activeFragmentRangeStart = -1;

  public CharSeqComposite(CharSequence... fragments) {
    this.fragments = compact(Arrays.asList(fragments)).toArray(new CharSequence[fragments.length]);
  }

  protected List<CharSequence> compact(List<CharSequence> fragments) {
    int fragmentsCount = fragments.size();
    for (final CharSequence fragment : fragments) {
      if (fragment instanceof CharSeqComposite) {
        final CharSeqComposite charSeqComposite = (CharSeqComposite) fragment;
        fragmentsCount += charSeqComposite.fragmentsCount() - 1;
      }
    }
    if (fragmentsCount == fragments.size()) {
      return fragments;
    }

    final List<CharSequence> compacted = new ArrayList<CharSequence>(fragmentsCount);
    for (int i = 0; i < fragments.size(); i++) {
      final CharSequence fragment = fragment(i);
      if (fragment instanceof CharSeqComposite) {
        final CharSeqComposite charSeqComposite = (CharSeqComposite) fragment;
        for (int j = 0; j < charSeqComposite.fragmentsCount(); j++) {
          compacted.add(charSeqComposite.fragment(j));
        }
      }
      else compacted.add(fragment);
    }
    return compacted;
  }

  public int length() {
    int length = 0;
    for (int i = 0; i < fragmentsCount(); i++) {
      length += fragment(i).length();
    }
    return length;
  }

  public char charAt(int offset) {
    if (fragmentsCount() == 1) {
      return fragment(0).charAt(offset);
    }
    final int offsetInActiveFragment = offset - activeFragmentRangeStart;
    if (activeFragment != null && offsetInActiveFragment >= 0 && offsetInActiveFragment < activeFragment.length()) {
      return activeFragment.charAt(offsetInActiveFragment);
    }

    int i = 0, fragmentEndOffset = 0;
    while (offset >= fragmentEndOffset) {
      fragmentEndOffset += fragment(i++).length();
    }

    activeFragmentRangeStart = fragmentEndOffset - fragment(i - 1).length();
    activeFragment = fragment(i - 1);
    return activeFragment.charAt(offset - activeFragmentRangeStart);
  }

  public CharSeq sub(int start, int end) {
    if (start == end) {
      return EMPTY;
    }
    final List<CharSequence> subSequenceFragments = new ArrayList<CharSequence>();
    int i = 0;
    int fragmentEndOffset = 0;

    while (start >= fragmentEndOffset) {
      fragmentEndOffset += fragment(i++).length();
    }
    int fragmentStartOffset = fragmentEndOffset - fragment(i - 1).length();

    if (fragmentEndOffset >= end) {
      final CharSequence result = fragment(i - 1).subSequence(start - fragmentStartOffset, end - fragmentStartOffset);
      if (result instanceof CharSeq)
        return (CharSeq) result;
      return new CharSeqAdapter(result);
    }

    final CharSequence startFragment = fragment(i - 1);
    subSequenceFragments.add(startFragment.subSequence(start - fragmentStartOffset, startFragment.length()));

    while (true) {
      final CharSequence fragment = fragment(i++);
      fragmentEndOffset += fragment.length();
      if (end <= fragmentEndOffset) {
        break;
      }
      subSequenceFragments.add(fragment);
    }
    fragmentStartOffset = (fragmentEndOffset - fragment(i - 1).length());
    subSequenceFragments.add(fragment(i - 1).subSequence(0, end - fragmentStartOffset));

    return new CharSeqBuilder(subSequenceFragments);
  }

  public char[] toCharArray() {
    final int length = length();
    final char[] chars = new char[length];
    copyToArray(0, chars, 0, length);
    fragments = new CharSequence[]{activeFragment = create(chars)};
    activeFragmentRangeStart = 0;
    return chars;
  }

  public void copyToArray(int start, char[] array, int offset, int length) {
    if (length == 0) {
      return;
    }
    int fragmentEnd = 0;
    int end = start + length;
    int index = 0;
    while (fragmentEnd <= start) {
      fragmentEnd += fragment(index++).length();
    }
    int fragmentLength = fragment(index - 1).length();
    int fragmentStart = fragmentEnd - fragmentLength;
    if (end <= fragmentEnd) {
      copyToArray(fragment(index - 1), start - fragmentStart, array, offset, end - fragmentStart);
      return;
    }

    final int toCopy = fragmentLength - (start - fragmentStart);
    copyToArray(fragment(index - 1), start - fragmentStart, array, offset, toCopy);
    offset += toCopy;
    while (true) {
      final CharSequence fragment = fragment(index++);
      fragmentLength = fragment.length();
      fragmentEnd += fragmentLength;
      if (fragmentEnd >= end) {
        break;
      }
      copyToArray(fragment, 0, array, offset, fragmentLength);
      offset += fragmentLength;
    }
    fragmentStart = fragmentEnd - fragmentLength;
    copyToArray(fragment(index - 1), 0, array, offset, end - fragmentStart);
  }

  private void copyToArray(CharSequence fragment, int startInFragment, char[] array, int startInArray, int length) {
    if (fragment instanceof CharSeq) {
      ((CharSeq) fragment).copyToArray(startInFragment, array, startInArray, length);
    } else {
      int index = 0;
      while (index++ < length) {
        array[startInArray++] = fragment.charAt(startInFragment++);
      }
    }
  }

  public int fragmentsCount() {
    return fragments.length;
  }

  public CharSequence fragment(final int j) {
    return fragments[j];
  }

  int hashCode;
  @Override
  public int hashCode() {
    if (hashCode == 0)
      hashCode = super.hashCode();
    return hashCode;
  }
}
