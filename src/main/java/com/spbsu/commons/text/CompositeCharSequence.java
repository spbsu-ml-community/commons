package com.spbsu.commons.text;

public class CompositeCharSequence extends CharSequenceBase {
  private CharSequence[] fragments;
  private int fragmentsCount;
  private CharSequence activeFragment = null;

  private int activeFragmentRangeStart = -1;

  public CompositeCharSequence(CharSequence... fragments) {
    fragmentsCount = fragments.length;
    this.fragments = compact(fragments);
  }

  public CompositeCharSequence(CharSequence[] fragments, int fragmentsCount) {
    this.fragmentsCount = fragmentsCount;
    this.fragments = compact(fragments);
  }

  private CharSequence[] compact(CharSequence[] fragments) {
    final int oldFragmentsCount = fragmentsCount;
    for (int i = 0; i < oldFragmentsCount; i++) {
      final CharSequence fragment = fragments[i];
      if (fragment instanceof CompositeCharSequence) {
        final CompositeCharSequence compositeCharSequence = (CompositeCharSequence) fragment;
        fragmentsCount += compositeCharSequence.fragmentsCount - 1;
      }
    }
    if (fragmentsCount == oldFragmentsCount) {
      return fragments;
    }

    final CharSequence[] compacted = new CharSequence[fragmentsCount];
    int index = 0;
    for (int i = 0; i < oldFragmentsCount; i++) {
      final CharSequence fragment = fragments[i];
      if (fragment instanceof CompositeCharSequence) {
        final CompositeCharSequence compositeCharSequence = (CompositeCharSequence) fragment;
        for (int j = 0; j < compositeCharSequence.fragmentsCount; j++) {
          compacted[index++] = compositeCharSequence.fragments[j];
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

  public CharSequence subSequence(int start, int end) {
    if (start == end) {
      return EMPTY;
    }
    final CharSequence[] subSequenceFragments = new CharSequence[fragmentsCount];
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

    final CharSequence startFragment = fragments[i - 1];
    subSequenceFragments[subSequenceFragmentsCount++] = startFragment.subSequence(start - fragmentStartOffset, startFragment.length());

    while (true) {
      final CharSequence fragment = fragments[i++];
      fragmentEndOffset += fragment.length();
      if (end <= fragmentEndOffset) {
        break;
      }
      subSequenceFragments[subSequenceFragmentsCount++] = fragment;
    }
    fragmentStartOffset = (fragmentEndOffset - fragments[i - 1].length());
    subSequenceFragments[subSequenceFragmentsCount++] = fragments[i - 1].subSequence(0, end - fragmentStartOffset);

    return new CompositeCharSequence(subSequenceFragments, subSequenceFragmentsCount);
  }

  public char[] toCharArray() {
    final int length = length();
    final char[] chars = new char[length];
    copyToArray(0, chars, 0, length);
    fragments = new CharSequence[]{create(chars)};
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
    if (fragment instanceof CharSequenceBase) {
      ((CharSequenceBase) fragment).copyToArray(startInFragment, array, startInArray, length);
    } else {
      int index = 0;
      while (index++ < length) {
        array[startInArray++] = fragment.charAt(startInFragment++);
      }
    }
  }

  public CharSequence[] getFragments() {
    return fragments;
  }

  public int getFragmentsCount() {
    return fragmentsCount;
  }
}
