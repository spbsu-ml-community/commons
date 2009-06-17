package com.spbsu.util;

public class CompositeCharSequence extends CharSequenceBase {
  private CharSequence[] fragments;
  private int fragmentsCount;
  private CharSequence activeFragment = null;

  private int activeFragmentRangeStart = -1;
  public CompositeCharSequence(final CharSequence... fragments) {
    fragmentsCount = fragments.length;
    this.fragments = compact(fragments);
  }

  public CompositeCharSequence(final CharSequence[] fragments, final int fragmentsCount) {
    this.fragmentsCount = fragmentsCount;
    this.fragments = compact(fragments);
  }

  private CharSequence[] compact(final CharSequence[] fragments) {
    final int oldFragmentsCount = fragmentsCount;
    CharArrayCharSequence prev = null;
    int prevPos = -1;
    for (int i = 0; i < oldFragmentsCount; i++) {
      final CharSequence fragment = fragments[i];
      if(fragment instanceof CharArrayCharSequence) {
        final CharArrayCharSequence sequence = (CharArrayCharSequence) fragment;
        if(prev != null && prev.array == sequence.array && prev.end == sequence.start) { // merge consequent parts
          fragments[prevPos] = prev = new CharArrayCharSequence(prev.array, prev.start, sequence.end);
          fragments[i] = null;
        }
        else {
          prev = sequence;
          prevPos = i;
        }
      }
    }

    boolean needCompaction = false;
    for (int i = 0; i < oldFragmentsCount; i++) {
      final CharSequence fragment = fragments[i];
      if(fragment instanceof CompositeCharSequence) {
        final CompositeCharSequence compositeCharSequence = (CompositeCharSequence) fragment;
        if (compositeCharSequence.fragmentsCount == 1)
          fragments[i] = compositeCharSequence.fragments[0];
        else needCompaction = true;
        fragmentsCount += compositeCharSequence.fragmentsCount - 1;
      }
      else if(fragment == null) {
        fragmentsCount--;
        needCompaction = true;
      }
    }

    if(!needCompaction) return fragments;

    final CharSequence[] compacted = new CharSequence[fragmentsCount];
    int index = 0;
    for (int i = 0; i < oldFragmentsCount; i++) {
      final CharSequence fragment = fragments[i];
      if(fragment instanceof CompositeCharSequence) {
        final CompositeCharSequence compositeCharSequence = (CompositeCharSequence) fragment;
        for (int j = 0; j < compositeCharSequence.fragmentsCount; j++) {
           compacted[index++] = compositeCharSequence.fragments[j];
        }
      }
      else if (fragment != null)
        compacted[index++] = fragment;
    }
    return compacted;
  }

  public int length() {
    int length = 0;
    final CharSequence[] fragments = this.fragments; // optimization, do not inline
    for (int i = fragmentsCount - 1; i >= 0; i--) {
      length += fragments[i].length();
    }
    return length;
  }

  public char charAt(final int offset) {
    final CharSequence[] fragments = this.fragments; // optimization, do not inline

    if(fragmentsCount == 1) return fragments[0].charAt(offset);
    final int offsetInActiveFragment = offset - activeFragmentRangeStart;
    if(activeFragment != null && offsetInActiveFragment >= 0 && offsetInActiveFragment < activeFragment.length())
      return activeFragment.charAt(offsetInActiveFragment);

    int i = 0;
    int fragmentEndOffset = 0;
    while(offset >= fragmentEndOffset) fragmentEndOffset += fragments[i++].length();

    activeFragmentRangeStart = fragmentEndOffset - fragments[i - 1].length();
    activeFragment = fragments[i - 1];
    return activeFragment.charAt(offset - activeFragmentRangeStart);
  }

  public CharSequence subSequence(final int start, final int end) {
    if(start == end) return CharSequenceBase.EMPTY;
    final CharSequence[] subSequenceFragments = new CharSequence[fragmentsCount];
    int subSequenceFragmentsCount = 0;
    int i = 0;
    int fragmentEndOffset = 0;

    while(start >= fragmentEndOffset) fragmentEndOffset += fragments[i++].length();
    int fragmentStartOffset = fragmentEndOffset - fragments[i - 1].length();

    if(fragmentEndOffset >= end)
      return fragments[i - 1].subSequence(start - fragmentStartOffset, end - fragmentStartOffset);

    final CharSequence startFragment = fragments[i - 1];
    subSequenceFragments[subSequenceFragmentsCount++] = startFragment.subSequence(start - fragmentStartOffset, startFragment.length());

    while(true){
      final CharSequence fragment = fragments[i++];
      fragmentEndOffset += fragment.length();
      if(end <= fragmentEndOffset) break;
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
    fragments = new CharSequence[]{CharSequenceBase.create(chars)};
    fragmentsCount = 1;
    activeFragment = fragments[0];
    activeFragmentRangeStart = 0;
    return chars;
  }

  public void copyToArray(final int start, final char[] array, int offset, final int length) {
    if(length == 0) return;
    int fragmentEnd = 0;
    final int end = start + length;
    int index = 0;
    while(fragmentEnd <= start) fragmentEnd += fragments[index++].length();
    int fragmentLength = fragments[index - 1].length();
    int fragmentStart = fragmentEnd - fragmentLength;
    if(end <= fragmentEnd){
      copyToArray(fragments[index - 1], start - fragmentStart, array, offset, end - fragmentStart);
      return;
    }

    final int toCopy = fragmentLength - (start - fragmentStart);
    copyToArray(fragments[index - 1], start - fragmentStart, array, offset, toCopy);
    offset += toCopy;
    while(true){
      final CharSequence fragment = fragments[index++];
      fragmentLength = fragment.length();
      fragmentEnd += fragmentLength;
      if(fragmentEnd >= end) break;
      copyToArray(fragment, 0, array, offset, fragmentLength);
      offset += fragmentLength;
    }
    fragmentStart = fragmentEnd - fragmentLength;
    copyToArray(fragments[index - 1], 0, array, offset, end - fragmentStart);
  }

  private void copyToArray(final CharSequence fragment, int startInFragment, final char[] array, int startInArray, final int length) {
    if(fragment instanceof CharSequenceBase){
      ((CharSequenceBase) fragment).copyToArray(startInFragment, array, startInArray, length);
    }
    else {
      int index = 0;
      while(index++ < length) {
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

  public boolean equals(final Object object) {
    if (object == this) return true;

    if (object instanceof CompositeCharSequence) {
      final CharSequence[] fragments = this.fragments;
      final int fragmentsCount = this.fragmentsCount;

      final CompositeCharSequence other = (CompositeCharSequence) object;
      final CharSequence[] otherFragments = other.fragments;
      final int otherFragmentsCount = other.fragmentsCount;

      int fragmentIndex = 0;
      int otherFragmentIndex = 0;

      int charIndex = 0;
      int otherCharIndex = 0;

      if (!(fragmentIndex < fragmentsCount && otherFragmentIndex < otherFragmentsCount)) return false;

      while (fragmentIndex < fragmentsCount && otherFragmentIndex < otherFragmentsCount) {
        final CharSequence fragment = fragments[fragmentIndex];
        final CharSequence otherFragment = otherFragments[otherFragmentIndex];

        final int fragmentLength = fragment.length();
        final int otherFragmentLength = otherFragment.length();

        while (charIndex < fragmentLength && otherCharIndex < otherFragmentLength) {
          if (fragment.charAt(charIndex) != otherFragment.charAt(otherCharIndex)) return false;
          ++charIndex;
          ++otherCharIndex;
        }

        if (charIndex == fragmentLength) {
          charIndex = 0;
          fragmentIndex++;
        }
        if (otherCharIndex == otherFragmentLength) {
          otherCharIndex = 0;
          otherFragmentIndex++;
        }
      }

      return fragmentIndex == fragmentsCount && otherFragmentIndex == otherFragmentsCount;
    }
    else return super.equals(object);
  }
}
