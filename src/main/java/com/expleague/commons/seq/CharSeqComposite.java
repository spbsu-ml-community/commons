package com.expleague.commons.seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CharSeqComposite extends CharSeq {
  public static final int MAXIMUM_COPY_FRAGMENT_LENGTH = 100;
  private CharSequence[] fragments;
  protected CharSequence activeFragment = null;

  protected int activeFragmentRangeStart = -1;
  protected int activeFragmentRangeEnd = -1;

  public CharSeqComposite(final CharSequence... fragments) {
    this.fragments = CharSeqTools.discloseComposites(fragments);
  }

  int length = -1;

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
    seek(offset);
    return activeFragment.charAt(offset - activeFragmentRangeStart);
  }

  private void seek(int offset) {
    int i = 0, fragmentEndOffset = 0;
    while (offset >= fragmentEndOffset) {
      fragmentEndOffset += fragment(i++).length();
    }

    final CharSequence fragment = fragment(i - 1);
    activeFragmentRangeStart = fragmentEndOffset - fragment.length();
    activeFragmentRangeEnd = fragmentEndOffset;
    activeFragment = fragment;
  }

  @Override
  public CharSeq sub(final int start, final int end) {
    if (end > length())
      throw new ArrayIndexOutOfBoundsException();
    if (start == end)
      return EMPTY;
    seek(start);
    if (activeFragmentRangeStart <= start && activeFragmentRangeEnd >= end) // fragment contains full range
      return create(activeFragment.subSequence(start - activeFragmentRangeStart, end - activeFragmentRangeStart));
    if (isImmutable() && end - start < MAXIMUM_COPY_FRAGMENT_LENGTH) { // fragment is small and it is easier to copy it
      final char[] copy = new char[end - start];
      copyToArray(start, copy, 0, end - start);
      return create(copy);
    }
    int index = 0;
    int fragmentEndOffset = 0;
    final List<CharSequence> subSequenceFragments = new ArrayList<>(fragmentsCount());
    while (start >= fragmentEndOffset) {
      fragmentEndOffset += fragment(index++).length();
    }
    final CharSequence firstNotEmpty = fragment(index - 1);
    int fragmentStartOffset = fragmentEndOffset - firstNotEmpty.length();
    subSequenceFragments.add(firstNotEmpty.subSequence(start - fragmentStartOffset, firstNotEmpty.length()));

    CharSequence current;
    while (true) {
      current = fragment(index++);
      fragmentEndOffset += current.length();
      if (end <= fragmentEndOffset)
        break;
      subSequenceFragments.add(current);
    }
    fragmentStartOffset = (fragmentEndOffset - current.length());
    subSequenceFragments.add(end - fragmentStartOffset < current.length() ? current.subSequence(0, end - fragmentStartOffset) : current);

    return new CharSeqComposite(subSequenceFragments.toArray(new CharSequence[subSequenceFragments.size()]));
  }

  @Override
  public char[] toCharArray() {
    final int length = length();
    final char[] chars = new char[length];
    copyToArray(0, chars, 0, length);
    if (isImmutable()) {
      fragments = new CharSequence[]{activeFragment = create(chars)};
      activeFragmentRangeStart = 0;
      activeFragmentRangeStart = activeFragment.length();
    }
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
//      if (!new CharSeqArray(array, offset, offset + fragmentLength).equals(fragment)) {
//        System.out.println(new CharSeqArray(array, offset, offset + fragmentLength).equals(fragment));
//      }
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
    try {
      return fragments[j];
    }
    catch (ArrayIndexOutOfBoundsException aioobe) {
      throw new ArrayIndexOutOfBoundsException("Invalid fragment " + j + " in composte: " +
          printStructure());
    }
  }

  private String printStructure() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < fragments.length; i++) {
      CharSequence fragment = fragments[i];
      if (i != 0) {
        builder.append(',');
      }
      builder.append(fragment.getClass()).append('[');
      if (fragment instanceof CharSeqComposite) {
        builder.append(((CharSeqComposite) fragment).printStructure());
      }
      else {
        builder.append(fragment.toString());
      }
      builder.append(']');
    }
    builder.append(']');
    return builder.toString();
  }

  int hashCode;
  @Override
  public int hashCode() {
    if (!isImmutable() || hashCode == 0)
      hashCode = super.hashCode();
    return hashCode;
  }
}
