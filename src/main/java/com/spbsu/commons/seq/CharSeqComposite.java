package com.spbsu.commons.seq;

public class CharSeqComposite extends CharSeq {
  private CharSeq[] fragments;
  private int fragmentsCount;
  private CharSeq activeFragment = null;

  private int activeFragmentRangeStart = -1;

  public CharSeqComposite(CharSequence... fragments) {
    this(fragments, fragments.length);
  }

  public CharSeqComposite(CharSequence[] fragments, int fragmentsCount) {
    this.fragmentsCount = fragmentsCount;
    CharSeq[] adapters = new CharSeq[fragmentsCount];
    for (int i = 0; i < adapters.length; i++) {
      adapters[i] = new CharSeqAdapter(fragments[i]);
    }
    this.fragments = compact(adapters);
  }

  private CharSeq[] compact(CharSeq[] fragments) {
    final int oldFragmentsCount = fragmentsCount;
    for (int i = 0; i < oldFragmentsCount; i++) {
      final CharSeq fragment = fragments[i];
      if (fragment instanceof CharSeqComposite) {
        final CharSeqComposite charSeqComposite = (CharSeqComposite) fragment;
        fragmentsCount += charSeqComposite.fragmentsCount - 1;
      }
    }
    if (fragmentsCount == oldFragmentsCount) {
      return fragments;
    }

    final CharSeq[] compacted = new CharSeq[fragmentsCount];
    int index = 0;
    for (int i = 0; i < oldFragmentsCount; i++) {
      final CharSeq fragment = fragments[i];
      if (fragment instanceof CharSeqComposite) {
        final CharSeqComposite charSeqComposite = (CharSeqComposite) fragment;
        for (int j = 0; j < charSeqComposite.fragmentsCount; j++) {
          compacted[index++] = charSeqComposite.fragments[j];
        }
      } else {
        compacted[index++] = fragment;
      }
    }
    return compacted;
  }

  public int length() {
    int length = 0;
    for (int i = 0; i < fragmentsCount; i++) {
      length += fragments[i].length();
    }
    return length;
  }

  public char charAt(int offset) {
    if (fragmentsCount == 1) {
      return fragments[0].charAt(offset);
    }
    final int offsetInActiveFragment = offset - activeFragmentRangeStart;
    if (activeFragment != null && offsetInActiveFragment >= 0 && offsetInActiveFragment < activeFragment.length()) {
      return activeFragment.charAt(offsetInActiveFragment);
    }

    int i = 0, fragmentEndOffset = 0;
    while (offset >= fragmentEndOffset) {
      fragmentEndOffset += fragments[i++].length();
    }

    activeFragmentRangeStart = fragmentEndOffset - fragments[i - 1].length();
    activeFragment = fragments[i - 1];
    return activeFragment.charAt(offset - activeFragmentRangeStart);
  }

  public CharSeq sub(int start, int end) {
    if (start == end) {
      return EMPTY;
    }
    final CharSeq[] subSequenceFragments = new CharSeq[fragmentsCount];
    int subSequenceFragmentsCount = 0;
    int i = 0;
    int fragmentEndOffset = 0;

    while (start >= fragmentEndOffset) {
      fragmentEndOffset += fragments[i++].length();
    }
    int fragmentStartOffset = fragmentEndOffset - fragments[i - 1].length();

    if (fragmentEndOffset >= end) {
      return fragments[i - 1].subSequence(start - fragmentStartOffset, end - fragmentStartOffset);
    }

    final CharSeq startFragment = fragments[i - 1];
    subSequenceFragments[subSequenceFragmentsCount++] = startFragment.subSequence(start - fragmentStartOffset, startFragment.length());

    while (true) {
      final CharSeq fragment = fragments[i++];
      fragmentEndOffset += fragment.length();
      if (end <= fragmentEndOffset) {
        break;
      }
      subSequenceFragments[subSequenceFragmentsCount++] = fragment;
    }
    fragmentStartOffset = (fragmentEndOffset - fragments[i - 1].length());
    subSequenceFragments[subSequenceFragmentsCount++] = fragments[i - 1].subSequence(0, end - fragmentStartOffset);

    return new CharSeqComposite(subSequenceFragments, subSequenceFragmentsCount);
  }

  public char[] toCharArray() {
    final int length = length();
    final char[] chars = new char[length];
    copyToArray(0, chars, 0, length);
    fragments = new CharSeq[]{create(chars)};
    fragmentsCount = 1;
    activeFragment = fragments[0];
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
      fragmentEnd += fragments[index++].length();
    }
    int fragmentLength = fragments[index - 1].length();
    int fragmentStart = fragmentEnd - fragmentLength;
    if (end <= fragmentEnd) {
      copyToArray(fragments[index - 1], start - fragmentStart, array, offset, end - fragmentStart);
      return;
    }

    final int toCopy = fragmentLength - (start - fragmentStart);
    copyToArray(fragments[index - 1], start - fragmentStart, array, offset, toCopy);
    offset += toCopy;
    while (true) {
      final CharSequence fragment = fragments[index++];
      fragmentLength = fragment.length();
      fragmentEnd += fragmentLength;
      if (fragmentEnd >= end) {
        break;
      }
      copyToArray(fragment, 0, array, offset, fragmentLength);
      offset += fragmentLength;
    }
    fragmentStart = fragmentEnd - fragmentLength;
    copyToArray(fragments[index - 1], 0, array, offset, end - fragmentStart);
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

  public CharSequence[] fragments() {
    return fragments;
  }

  public int fragmentsCount() {
    return fragmentsCount;
  }
}
