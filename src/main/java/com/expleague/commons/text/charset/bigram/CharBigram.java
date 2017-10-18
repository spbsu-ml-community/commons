package com.expleague.commons.text.charset.bigram;

import org.jetbrains.annotations.NotNull;

/**
 * @author lyadzhin
 */
public class CharBigram implements Comparable<CharBigram> {
  public static CharBigram valueOf(final char c1, final char c2) {
    return new CharBigram(c1, c2); //TODO: IMPLEMENT CACHING
  }

  /**
   * Constructs bigram of first two characters of CharSequence.
   * Throws runtime exception if string length is less than 2.
   */
  public static CharBigram valueOf(@NotNull final CharSequence s) {
    if (s.length() < 2) {
      throw new IllegalArgumentException("s length must be >= 2");
    }
    return valueOf(s.charAt(0), s.charAt(1));
  }

  private final char c1;
  private final char c2;
  private int hashCode = -1;

  private CharBigram(final char c1, final char c2) {
    this.c1 = c1;
    this.c2 = c2;
  }

  public char getFirstChar() {
    return c1;
  }

  public char getLastChar() {
    return c2;
  }

  @Override
  public int compareTo(@NotNull final CharBigram o) {
    if (c1 < o.c1) {
      return -1;
    } else if (c1 > o.c1) {
      return 1;
    } else {
      if (c2 < o.c2) {
        return -1;
      } else if (c2 > o.c2) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CharBigram)) {
      return false;
    }
    final CharBigram that = (CharBigram) o;
    return this.c1 == that.c1 && this.c2 == that.c2;
  }

  @Override
  public int hashCode() {
    int hashCode = this.hashCode;
    if (hashCode == -1) {
      hashCode = (c1 << 31) | c2;
      this.hashCode = hashCode;
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return "" + c1 + c2;
  }
}
