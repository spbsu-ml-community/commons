package com.spbsu.commons.seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("EqualsAndHashcode")
public class CharSeqComposite extends CharSeq {
  private CharSequence[] fragments;
  protected CharSequence activeFragment = null;

  protected int activeFragmentRangeStart = -1;

  public CharSeqComposite(final CharSequence... fragments) {
    this.fragments = CharSeqTools.discloseComposites(Arrays.asList(fragments)).toArray(new CharSequence[fragments.length]);
  }

  int length = -1;

  public CharSeqComposite(final CharSequence[] parts, final int start, final int end) {
    fragments = CharSeqTools.discloseComposites(Arrays.asList(parts).subList(start, end)).toArray(new CharSequence[end - start]);
  }

  public int length() {
    if (isImmutable() && length >= 0)
      return length;
    int length = 0;
    for (int i = 0; i < fragmentsCount(); i++) {
      length += fragment(i).length();
    }
    return this.length = length;
  }

  public char charAt(final int offset) {
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

  public CharSeq sub(final int start, final int end) {
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

    return new CharSeqComposite(subSequenceFragments.toArray(new CharSequence[subSequenceFragments.size()]));
  }

  public char[] toCharArray() {
    final int length = length();
    final char[] chars = new char[length];
    copyToArray(0, chars, 0, length);
    fragments = new CharSequence[]{activeFragment = create(chars)};
    activeFragmentRangeStart = 0;
    return chars;
  }

  public void copyToArray(final int start, final char[] array, int offset, final int length) {
    if (length == 0) {
      return;
    }
    int fragmentEnd = 0;
    final int end = start + length;
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

  private void copyToArray(final CharSequence fragment, int startInFragment, final char[] array, int startInArray, final int length) {
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
    if (!isImmutable() || hashCode == 0)
      hashCode = super.hashCode();
    return hashCode;
  }
}
