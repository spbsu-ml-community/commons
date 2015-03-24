package com.spbsu.commons.seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("EqualsAndHashcode")
public class CharSeqComposite extends CharSeq {
  private CharSequence[] fragments;
  protected CharSequence activeFragment = null;

  protected int activeFragmentRangeStart = -1;
  protected int activeFragmentRangeEnd = -1;

  public CharSeqComposite(final CharSequence... fragments) {
    this.fragments = CharSeqTools.discloseComposites(Arrays.asList(fragments)).toArray(new CharSequence[fragments.length]);
  }

  int length = -1;

  public CharSeqComposite(final CharSequence[] parts, final int start, final int end) {
    fragments = CharSeqTools.discloseComposites(Arrays.asList(parts).subList(start, end)).toArray(new CharSequence[end - start]);
  }

  @Override
  public int length() {
    if (isImmutable() && length >= 0)
      return length;
    int length = 0;
    for (int i = 0; i < fragmentsCount(); i++) {
      length += fragment(i).length();
    }
    return this.length = length;
  }

  @Override
  public char charAt(final int offset) {
    if (offset >= activeFragmentRangeStart && offset < activeFragmentRangeEnd)
      return activeFragment.charAt(offset - activeFragmentRangeStart);

    return heavyAt(offset);
  }

  private char heavyAt(int offset) {
    int i = 0, fragmentEndOffset = 0;
    while (offset >= fragmentEndOffset) {
      fragmentEndOffset += fragment(i++).length();
    }

    activeFragmentRangeStart = fragmentEndOffset - fragment(i - 1).length();
    activeFragmentRangeEnd = fragmentEndOffset;
    activeFragment = fragment(i - 1);
    return activeFragment.charAt(offset - activeFragmentRangeStart);
  }

  @Override
  public CharSeq sub(final int start, final int end) {
    if (end > length())
      throw new ArrayIndexOutOfBoundsException();
    if (start == end)
      return EMPTY;
    if (end - start < length() / fragments.length)
      return new CharSeqAdapter(this, start, end);
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

  @Override
  public char[] toCharArray() {
    final int length = length();
    final char[] chars = new char[length];
    copyToArray(0, chars, 0, length);
    fragments = new CharSequence[]{activeFragment = create(chars)};
    activeFragmentRangeStart = 0;
    activeFragmentRangeStart = activeFragment.length();
    return chars;
  }

  @Override
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
    final int copyStartInFragment = start - fragmentStart;
    if (end <= fragmentEnd) {
      copyToArray(fragment(index - 1), copyStartInFragment, array, offset, length);
      return;
    }

    final int toCopy = fragmentLength - copyStartInFragment;
    copyToArray(fragment(index - 1), copyStartInFragment, array, offset, toCopy);
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
    if (array.length < startInArray + length)
      throw new ArrayIndexOutOfBoundsException(startInArray + length);
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
