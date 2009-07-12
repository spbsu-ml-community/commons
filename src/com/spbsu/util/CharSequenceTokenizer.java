package com.spbsu.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This is a copy of {@link java.util.StringTokenizer} for convenient use
 * with {@link CharSequence}
 * @author vp
 */
public class CharSequenceTokenizer implements Enumeration<CharSequence> {
  private int currentPosition;
  private int newPosition;
  private int maxPosition;
  private CharSequence str;
  private String delimiters;
  private boolean retDelims;
  private boolean delimsChanged;

  /**
   * maxDelimCodePoint stores the value of the delimiter character with the
   * highest value. It is used to optimize the detection of delimiter
   * characters.
   *
   * It is unlikely to provide any optimization benefit in the
   * hasSurrogates case because most string characters will be
   * smaller than the limit, but we keep it so that the two code
   * paths remain similar.
   */
  private int maxDelimCodePoint;

  /**
   * If delimiters include any surrogates (including surrogate
   * pairs), hasSurrogates is true and the tokenizer uses the
   * different code path. This is because String.indexOf(int)
   * doesn't handle unpaired surrogates as a single character.
   */
  private boolean hasSurrogates = false;

  /**
   * When hasSurrogates is true, delimiters are converted to code
   * points and isDelimiter(int) is used to determine if the given
   * codepoint is a delimiter.
   */
  private int[] delimiterCodePoints;

  /**
   * Set maxDelimCodePoint to the highest char in the delimiter set.
   */
  private void setMaxDelimCodePoint() {
    if (delimiters == null) {
      maxDelimCodePoint = 0;
      return;
    }

    int m = 0;
    int c;
    int count = 0;
    for (int i = 0; i < delimiters.length(); i += Character.charCount(c)) {
      c = delimiters.charAt(i);
      if (c >= Character.MIN_HIGH_SURROGATE && c <= Character.MAX_LOW_SURROGATE) {
        c = codePointAt(delimiters, i);
        hasSurrogates = true;
      }
      if (m < c)
        m = c;
      count++;
    }
    maxDelimCodePoint = m;

    if (hasSurrogates) {
      delimiterCodePoints = new int[count];
      for (int i = 0, j = 0; i < count; i++, j += Character.charCount(c)) {
        c = codePointAt(delimiters, j);
        delimiterCodePoints[i] = c;
      }
    }
  }

  /**
   * Constructs a string tokenizer for the specified string. All
   * characters in the <code>delim</code> argument are the delimiters
   * for separating tokens.
   * <p>
   * If the <code>returnDelims</code> flag is <code>true</code>, then
   * the delimiter characters are also returned as tokens. Each
   * delimiter is returned as a string of length one. If the flag is
   * <code>false</code>, the delimiter characters are skipped and only
   * serve as separators between tokens.
   * <p>
   * Note that if <tt>delim</tt> is <tt>null</tt>, this constructor does
   * not throw an exception. However, trying to invoke other methods on the
   * resulting <tt>StringTokenizer</tt> may result in a
   * <tt>NullPointerException</tt>.
   *
   * @param   str            a string to be parsed.
   * @param   delim          the delimiters.
   * @param   returnDelims   flag indicating whether to return the delimiters
   *                         as tokens.
   * @exception NullPointerException if str is <CODE>null</CODE>
   */
  public CharSequenceTokenizer(final CharSequence str, final String delim, final boolean returnDelims) {
    currentPosition = 0;
    newPosition = -1;
    delimsChanged = false;
    this.str = str;
    maxPosition = str.length();
    delimiters = delim;
    retDelims = returnDelims;
    setMaxDelimCodePoint();
  }

  /**
   * Constructs a string tokenizer for the specified string. The
   * characters in the <code>delim</code> argument are the delimiters
   * for separating tokens. Delimiter characters themselves will not
   * be treated as tokens.
   * <p>
   * Note that if <tt>delim</tt> is <tt>null</tt>, this constructor does
   * not throw an exception. However, trying to invoke other methods on the
   * resulting <tt>StringTokenizer</tt> may result in a
   * <tt>NullPointerException</tt>.
   *
   * @param   str     a string to be parsed.
   * @param   delim   the delimiters.
   * @exception NullPointerException if str is <CODE>null</CODE>
   */
  public CharSequenceTokenizer(final CharSequence str, final String delim) {
    this(str, delim, false);
  }

  /**
   * Constructs a string tokenizer for the specified string. The
   * tokenizer uses the default delimiter set, which is
   * <code>"&nbsp;&#92;t&#92;n&#92;r&#92;f"</code>: the space character,
   * the tab character, the newline character, the carriage-return character,
   * and the form-feed character. Delimiter characters themselves will
   * not be treated as tokens.
   *
   * @param   str   a string to be parsed.
   * @exception NullPointerException if str is <CODE>null</CODE>
   */
  public CharSequenceTokenizer(final CharSequence str) {
    this(str, " \t\n\r\f", false);
  }

  /**
   * Skips delimiters starting from the specified position. If retDelims
   * is false, returns the index of the first non-delimiter character at or
   * after startPos. If retDelims is true, startPos is returned.
   */
  private int skipDelimiters(final int startPos) {
    if (delimiters == null)
      throw new NullPointerException();

    int position = startPos;
    while (!retDelims && position < maxPosition) {
      if (!hasSurrogates) {
        char c = str.charAt(position);
        if ((c > maxDelimCodePoint) || (delimiters.indexOf(c) < 0))
          break;
        position++;
      } else {
        int c = codePointAt(str, position);
        if ((c > maxDelimCodePoint) || !isDelimiter(c)) {
          break;
        }
        position += Character.charCount(c);
      }
    }
    return position;
  }

  /**
   * Skips ahead from startPos and returns the index of the next delimiter
   * character encountered, or maxPosition if no such delimiter is found.
   */
  private int scanToken(final int startPos) {
    int position = startPos;
    while (position < maxPosition) {
      if (!hasSurrogates) {
        char c = str.charAt(position);
        if ((c <= maxDelimCodePoint) && (delimiters.indexOf(c) >= 0))
          break;
        position++;
      } else {
        int c = codePointAt(str, position);
        if ((c <= maxDelimCodePoint) && isDelimiter(c))
          break;
        position += Character.charCount(c);
      }
    }
    if (retDelims && (startPos == position)) {
      if (!hasSurrogates) {
        char c = str.charAt(position);
        if ((c <= maxDelimCodePoint) && (delimiters.indexOf(c) >= 0))
          position++;
      } else {
        int c = codePointAt(str, position);
        if ((c <= maxDelimCodePoint) && isDelimiter(c))
          position += Character.charCount(c);
      }
    }
    return position;
  }

  private boolean isDelimiter(final int codePoint) {
    for (final int delimiterCodePoint : delimiterCodePoints) {
      if (delimiterCodePoint == codePoint) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests if there are more tokens available from this tokenizer's string.
   * If this method returns <tt>true</tt>, then a subsequent call to
   * <tt>nextToken</tt> with no argument will successfully return a token.
   *
   * @return  <code>true</code> if and only if there is at least one token
   *          in the string after the current position; <code>false</code>
   *          otherwise.
   */
  public boolean hasMoreTokens() {
    /*
     * Temporarily store this position and use it in the following
     * nextToken() method only if the delimiters haven't been changed in
     * that nextToken() invocation.
     */
    newPosition = skipDelimiters(currentPosition);
    return (newPosition < maxPosition);
  }

  /**
   * Returns the next token from this string tokenizer.
   *
   * @return     the next token from this string tokenizer.
   * @exception java.util.NoSuchElementException  if there are no more tokens in this
   *               tokenizer's string.
   */
  public CharSequence nextToken() {
    /*
     * If next position already computed in hasMoreElements() and
     * delimiters have changed between the computation and this invocation,
     * then use the computed value.
     */

    currentPosition = (newPosition >= 0 && !delimsChanged) ?
      newPosition : skipDelimiters(currentPosition);

    /* Reset these anyway */
    delimsChanged = false;
    newPosition = -1;

    if (currentPosition >= maxPosition) throw new NoSuchElementException();

    final int start = currentPosition;
    currentPosition = scanToken(currentPosition);
    return str.subSequence(start, currentPosition);
  }

  /**
   * Returns the next token in this string tokenizer's string. First,
   * the set of characters considered to be delimiters by this
   * <tt>StringTokenizer</tt> object is changed to be the characters in
   * the string <tt>delim</tt>. Then the next token in the string
   * after the current position is returned. The current position is
   * advanced beyond the recognized token.  The new delimiter set
   * remains the default after this call.
   *
   * @param      delim   the new delimiters.
   * @return     the next token, after switching to the new delimiter set.
   * @exception  NoSuchElementException  if there are no more tokens in this
   *               tokenizer's string.
   * @exception NullPointerException if delim is <CODE>null</CODE>
   */
  public CharSequence nextToken(final String delim) {
    delimiters = delim;

    /* delimiter string specified, so set the appropriate flag. */
    delimsChanged = true;

    setMaxDelimCodePoint();
    return nextToken();
  }

  /**
   * Returns the same value as the <code>hasMoreTokens</code>
   * method. It exists so that this class can implement the
   * <code>Enumeration</code> interface.
   *
   * @return  <code>true</code> if there are more tokens;
   *          <code>false</code> otherwise.
   * @see     java.util.Enumeration
   * @see     java.util.StringTokenizer#hasMoreTokens()
   */
  public boolean hasMoreElements() {
    return hasMoreTokens();
  }

  /**
   * Returns the same value as the <code>nextToken</code> method,
   * except that its declared return value is <code>Object</code> rather than
   * <code>String</code>. It exists so that this class can implement the
   * <code>Enumeration</code> interface.
   *
   * @return     the next token in the string.
   * @exception  NoSuchElementException  if there are no more tokens in this
   *               tokenizer's string.
   * @see        java.util.Enumeration
   * @see        java.util.StringTokenizer#nextToken()
   */
  public CharSequence nextElement() {
    return nextToken();
  }

  /**
   * Calculates the number of times that this tokenizer's
   * <code>nextToken</code> method can be called before it generates an
   * exception. The current position is not advanced.
   *
   * @return  the number of tokens remaining in the string using the current
   *          delimiter set.
   * @see     java.util.StringTokenizer#nextToken()
   */
  public int countTokens() {
    int count = 0;
    int currpos = currentPosition;
    while (currpos < maxPosition) {
      currpos = skipDelimiters(currpos);
      if (currpos >= maxPosition)
        break;
      currpos = scanToken(currpos);
      count++;
    }
    return count;
  }

  private static int codePointAt(final CharSequence input, final int index) {
    return Character.codePointAt(input, index);
  }
}
