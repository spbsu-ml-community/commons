package com.expleague.commons.seq;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

/**
 * User: Igor Kuralenok
 * Date: 10.05.2006
 * Time: 17:55:23
 */
public abstract class CharSeq implements Seq<Character>, CharSequence, Comparable<CharSeq> {
  public static final CharSeq EMPTY = new CharSeq() {
    @Override
    public int length() {
      return 0;
    }
    @Override
    public char charAt(int offset) {
      throw new IllegalStateException();
    }
  };
  public static final Comparator<Seq<Character>> SEQ_COMPARATOR = CharSeqTools.lexicographicalComparator(Character.class);

  @Override
  public abstract char charAt(int offset);
  @Override
  public CharSeq sub(final int start, final int end) {
    if (start == 0 && end == length())
      return this;
    char[] copy = new char[end - start];
    copyToArray(start, copy, 0, end - start);
    return new CharSeqArray(copy);
  }

  @Override
  public IntStream stream() {
    return IntStream.generate(new IntSupplier() {
      int index = 0;
      @Override
      public int getAsInt() {
        return charAt(index++);
      }
    }).limit(length());
  }

  @Override
  public boolean isImmutable() {
    return true;
  }

  @Override
  public final CharSeq subSequence(final int start, final int end) {
    return sub(start, end);
  }

  public final CharSeq subSequence(final int start) {
    return subSequence(start, length());
  }

  @Override
  public final Character at(final int i) {
    return charAt(i);
  }

  public char last() {
    return length() > 0 ? charAt(length() - 1) : 0;
  }

  public boolean equals(final Object object) {
    if (object instanceof CharSequence) {
      final CharSequence str = (CharSequence) object;
      final int length = length();
      if (str.length() != length) {
        return false;
      }
      if (str.hashCode() != hashCode()) {
        return false;
      }
      int index = 0;
      while (index < length) {
        if (str.charAt(index) != charAt(index)) {
          return false;
        }
        index++;
      }
      return true;
    }
    return false;
  }

  @NotNull
  public String toString() {
    return new String(toCharArray());
  }

  public int hashCode() {
    final int len = length();
    int h = 0;
    for (int i = 0; i < len; i++) {
      h = 31 * h + charAt(i);
    }
    return h == 0 ? 1 : h;
  }

  public char[] toArray() {
    return toCharArray();
  }

  public char[] toCharArray() {
    final char[] chars = new char[length()];
    copyToArray(0, chars, 0, length());
    return chars;
  }

  public void copyToArray(int start, final char[] array, int offset, final int length) {
    int index = 0;
    while (index++ < length) {
      array[offset++] = charAt(start++);
    }
  }

  public CharSeq trim() {
    final int length = length();
    if (length == 0)
      return this;
    int nonWsStart = 0;
    int nonWsEnd = length;
    //noinspection StatementWithEmptyBody
    while (nonWsStart < length && CharSeqTools.isWhitespace(charAt(nonWsStart++))) ;
    //noinspection StatementWithEmptyBody
    nonWsStart--;
    while (--nonWsEnd >= 0 && CharSeqTools.isWhitespace(charAt(nonWsEnd))) ;
    nonWsEnd++;

    return nonWsStart < nonWsEnd ? subSequence(nonWsStart, nonWsEnd) : CharSeq.EMPTY;
  }

  public static CharSequence createArrayBasedSequence(final CharSequence text) {
    if (text instanceof CharSeqArray) {
      return text;
    }
    return allocateArrayBasedSequence(text);
  }

  public static CharSequence allocateArrayBasedSequence(final CharSequence text) {
    final int textLen = text.length();
    final char[] chars = new char[textLen];
    int index = 0;
    while (index < textLen) {
      chars[index] = text.charAt(index++);
    }
    return create(chars);
  }

  public static CharSeq create(final char[] text, final int start, final int end) {
    return end != start ? new CharSeqArray(text, start, end) : EMPTY;
  }

  public static CharSeq create(final char[] text) {
    return text.length == 0 ? EMPTY : new CharSeqArray(text, 0, text.length);
  }

  public static CharSeq copy(final char[] text) {
    return copy(text, 0, text.length);
  }

  public static CharSeq copy(final CharSequence text) {
    return copy(text, 0, text.length());
  }

  public static CharSeq copy(final CharSequence text, final int start, final int end) {
    final char[] copy = new char[end - start];
    if (text instanceof CharSeq) {
      ((CharSeq) text).copyToArray(start, copy, 0, end - start);
    }
    else {
      for (int i = 0; i < copy.length; i++) {
        copy[i] = text.charAt(i);
      }
    }
    return new CharSeqArray(copy);
  }

  public static CharSeq copy(final char[] text, final int start, final int end) {
    final char[] copy = new char[end - start];
    System.arraycopy(text, start, copy, 0, end - start);
    return new CharSeqArray(copy);
  }

  private static final Map<CharSeq, WeakReference<CharSeq>> intern = new WeakHashMap<>();
  public static synchronized CharSeq intern(CharSeq seq) {
    WeakReference<CharSeq> known = intern.get(seq);
    if (known != null) {
      CharSeq intern = known.get();
      if (intern != null) {
        return intern;
      }
    }
    CharSeq compact = compact(seq);
    intern.put(compact, new WeakReference<>(compact));
    return compact;
  }

  public static CharSeq compact(CharSequence seq) {
    if (CharSeqTools.isNumeric(seq)) { // compact integers representation
      try {
        if (seq.length() < 10) {
          try {
            return new CharSeqInt(CharSeqTools.parseInt(seq));
          }
          catch (NumberFormatException ignore) { // integer failure
            return new CharSeqLong(CharSeqTools.parseLong(seq));
          }
        }
        else {
          return new CharSeqLong(CharSeqTools.parseLong(seq));
        }
      }
      catch (NumberFormatException ignore) { // long failure
      }
    }
    {
      byte[] latin1Bytes = CharSeqTools.tryLatin1(seq);
      if (latin1Bytes != null) {
        return new CharSeqLatin1Array(latin1Bytes, 0, latin1Bytes.length);
      }
    }
    {
      byte[] compactBytes = CharSeqTools.tryCompactBytes(seq);
      if (compactBytes != null) {
        return new CharSeqByteArray(seq.charAt(0), compactBytes, 0, compactBytes.length);
      }
    }
    return CharSeq.copy(seq);
  }

  public static CharSeq create(final CharSequence string) {
    return string instanceof CharSeq ? (CharSeq)string : new CharSeqAdapter(string);
  }

  @Override
  public Class<Character> elementType() {
    return char.class;
  }

  public int indexOf(final char ch) {
    int index = 0;
    while (index < length() && at(index) != ch)
      index++;
    return index;
  }

  @Override
  public int compareTo(CharSeq o) {
    return SEQ_COMPARATOR.compare(this, o);
  }
}
