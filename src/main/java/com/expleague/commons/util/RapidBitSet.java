package com.expleague.commons.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.Arrays;

/**
 * Copy of java.util.BitSet with some performance tuning
 * @author vp
 */
public class RapidBitSet implements Cloneable, java.io.Serializable {
  /*
  * BitSets are packed into arrays of "words."  Currently a word is
  * a long, which consists of 64 bits, requiring 6 address bits.
  * The choice of word size is determined purely by performance concerns.
  */
  private final static int ADDRESS_BITS_PER_WORD = 6;
  private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
  private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;

  /* Used to shift left or right for a partial word mask */
  private static final long WORD_MASK = 0xffffffffffffffffL;

  /**
   * @serialField bits long[]
   * <p/>
   * The bits in this BitSet.  The ith bit is stored in bits[i/64] at
   * bit position i % 64 (where bit position 0 refers to the least
   * significant bit and 63 refers to the most significant bit).
   */
  private static final ObjectStreamField[] serialPersistentFields = {
    new ObjectStreamField("bits", long[].class),
  };

  /**
   * The internal field corresponding to the serialField "bits".
   */
  private long[] words;

  /**
   * The number of words in the logical size of this BitSet.
   */
  private transient int wordsInUse = 0;
  private int firstNonZero = Integer.MAX_VALUE;
  private int lastNonZero = 0;

  /**
   * Whether the size of "words" is user-specified.  If so, we assume
   * the user knows what he's doing and try harder to preserve it.
   */
  private transient boolean sizeIsSticky = false;

  /* use serialVersionUID from JDK 1.0.2 for interoperability */
  private static final long serialVersionUID = 7997698588986878753L;

  /**
   * Given a bit index, return word index containing it.
   */
  private static int wordIndex(final int bitIndex) {
    return bitIndex >> ADDRESS_BITS_PER_WORD;
  }

  /**
   * Every public method must preserve these invariants.
   */
  private void checkInvariants() {
    assert (wordsInUse == 0 || words[wordsInUse - 1] != 0);
    assert (wordsInUse >= 0 && wordsInUse <= words.length);
    assert (wordsInUse == words.length || words[wordsInUse] == 0);
  }

  /**
   * Set the field wordsInUse with the logical size in words of the bit
   * set.  WARNING:This method assumes that the number of words actually
   * in use is less than or equal to the current value of wordsInUse!
   */
  private void recalculateWordsInUse() {
    // Traverse the bitset until a used word is found
    int i;
    for (i = wordsInUse - 1; i >= 0; i--)
      if (words[i] != 0)
        break;

    wordsInUse = i + 1; // The new logical size
  }

  /**
   * Creates a new bit set. All bits are initially <code>false</code>.
   */
  public RapidBitSet() {
    initWords(BITS_PER_WORD);
    sizeIsSticky = false;
  }

  /**
   * Creates a bit set whose initial size is large enough to explicitly
   * represent bits with indices in the range <code>0</code> through
   * <code>nbits-1</code>. All bits are initially <code>false</code>.
   *
   * @param nbits the initial size of the bit set.
   * @throws NegativeArraySizeException if the specified initial size
   *                                    is negative.
   */
  public RapidBitSet(final int nbits) {
    // nbits can't be negative; size 0 is OK
    if (nbits < 0)
      throw new NegativeArraySizeException("nbits < 0: " + nbits);

    initWords(nbits);
    sizeIsSticky = true;
  }

  private void initWords(final int nbits) {
    words = new long[wordIndex(nbits - 1) + 1];
  }

  /**
   * Ensures that the BitSet can hold enough words.
   *
   * @param wordsRequired the minimum acceptable number of words.
   */
  private void ensureCapacity(final int wordsRequired) {
    if (words.length < wordsRequired) {
      // Allocate larger of doubled size or required size
      final int request = Math.max(2 * words.length, wordsRequired);
      words = Arrays.copyOf(words, request);
      sizeIsSticky = false;
    }
  }

  /**
   * Ensures that the BitSet can accommodate a given wordIndex,
   * temporarily violating the invariants.  The caller must
   * restore the invariants before returning to the user,
   * possibly using recalculateWordsInUse().
   *
   * @param  wordIndex the index to be accommodated.
   */
  private void expandTo(final int wordIndex) {
    final int wordsRequired = wordIndex + 1;
    if (wordsInUse < wordsRequired) {
      ensureCapacity(wordsRequired);
      wordsInUse = wordsRequired;
    }
  }

  /**
   * Checks that fromIndex ... toIndex is a valid range of bit indices.
   */
  private static void checkRange(final int fromIndex, final int toIndex) {
    if (fromIndex < 0)
      throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
    if (toIndex < 0)
      throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
    if (fromIndex > toIndex)
      throw new IndexOutOfBoundsException("fromIndex: " + fromIndex +
        " > toIndex: " + toIndex);
  }

  /**
   * Sets the bit at the specified index to the complement of its
   * current value.
   *
   * @param bitIndex the index of the bit to flip.
   * @throws IndexOutOfBoundsException if the specified index is negative.
   * @since 1.4
   */
  public void flip(final int bitIndex) {
    if (bitIndex < 0)
      throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

    final int wordIndex = wordIndex(bitIndex);
    expandTo(wordIndex);

    words[wordIndex] ^= (1L << bitIndex);

    recalculateWordsInUse();
    checkInvariants();
  }

  /**
   * Sets each bit from the specified <tt>fromIndex</tt> (inclusive) to the
   * specified <tt>toIndex</tt> (exclusive) to the complement of its current
   * value.
   *
   * @param fromIndex index of the first bit to flip.
   * @param toIndex   index after the last bit to flip.
   * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
   *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
   *                                   larger than <tt>toIndex</tt>.
   * @since 1.4
   */
  public void flip(final int fromIndex, final int toIndex) {
    checkRange(fromIndex, toIndex);

    if (fromIndex == toIndex)
      return;

    final int startWordIndex = wordIndex(fromIndex);
    final int endWordIndex = wordIndex(toIndex - 1);
    expandTo(endWordIndex);

    final long firstWordMask = WORD_MASK << fromIndex;
    final long lastWordMask = WORD_MASK >>> -toIndex;
    if (startWordIndex == endWordIndex) {
      // Case 1: One word
      words[startWordIndex] ^= (firstWordMask & lastWordMask);
    } else {
      // Case 2: Multiple words
      // Handle first word
      words[startWordIndex] ^= firstWordMask;

      // Handle intermediate words, if any
      for (int i = startWordIndex + 1; i < endWordIndex; i++)
        words[i] ^= WORD_MASK;

      // Handle last word
      words[endWordIndex] ^= lastWordMask;
    }

    recalculateWordsInUse();
    checkInvariants();
  }

  /**
   * Sets the bit at the specified index to <code>true</code>.
   *
   * @param bitIndex a bit index.
   * @throws IndexOutOfBoundsException if the specified index is negative.
   * @since JDK1.0
   */
  public void set(final int bitIndex) {
    if (bitIndex < 0)
      throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

    final int wordIndex = wordIndex(bitIndex);
    expandTo(wordIndex);

    words[wordIndex] |= (1L << bitIndex); // Restores invariants

    firstNonZero = Math.min(firstNonZero, wordIndex);
    lastNonZero = Math.max(lastNonZero, wordIndex);

    checkInvariants();
  }

  /**
   * Sets the bit at the specified index to the specified value.
   *
   * @param bitIndex a bit index.
   * @param value    a boolean value to set.
   * @throws IndexOutOfBoundsException if the specified index is negative.
   * @since 1.4
   */
  public void set(final int bitIndex, final boolean value) {
    if (value)
      set(bitIndex);
    else
      clear(bitIndex);
  }

  /**
   * Sets the bits from the specified <tt>fromIndex</tt> (inclusive) to the
   * specified <tt>toIndex</tt> (exclusive) to <code>true</code>.
   *
   * @param fromIndex index of the first bit to be set.
   * @param toIndex   index after the last bit to be set.
   * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
   *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
   *                                   larger than <tt>toIndex</tt>.
   * @since 1.4
   */
  public void set(final int fromIndex, final int toIndex) {
    checkRange(fromIndex, toIndex);

    if (fromIndex == toIndex)
      return;

    // Increase capacity if necessary
    final int startWordIndex = wordIndex(fromIndex);
    final int endWordIndex = wordIndex(toIndex - 1);
    expandTo(endWordIndex);

    final long firstWordMask = WORD_MASK << fromIndex;
    final long lastWordMask = WORD_MASK >>> -toIndex;
    if (startWordIndex == endWordIndex) {
      // Case 1: One word
      words[startWordIndex] |= (firstWordMask & lastWordMask);
    } else {
      // Case 2: Multiple words
      // Handle first word
      words[startWordIndex] |= firstWordMask;

      // Handle intermediate words, if any
      for (int i = startWordIndex + 1; i < endWordIndex; i++)
        words[i] = WORD_MASK;

      // Handle last word (restores invariants)
      words[endWordIndex] |= lastWordMask;
    }

    checkInvariants();
  }

  /**
   * Sets the bits from the specified <tt>fromIndex</tt> (inclusive) to the
   * specified <tt>toIndex</tt> (exclusive) to the specified value.
   *
   * @param fromIndex index of the first bit to be set.
   * @param toIndex   index after the last bit to be set
   * @param value     value to set the selected bits to
   * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
   *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
   *                                   larger than <tt>toIndex</tt>.
   * @since 1.4
   */
  public void set(final int fromIndex, final int toIndex, final boolean value) {
    if (value)
      set(fromIndex, toIndex);
    else
      clear(fromIndex, toIndex);
  }

  /**
   * Sets the bit specified by the index to <code>false</code>.
   *
   * @param bitIndex the index of the bit to be cleared.
   * @throws IndexOutOfBoundsException if the specified index is negative.
   * @since JDK1.0
   */
  public void clear(final int bitIndex) {
    if (bitIndex < 0)
      throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

    final int wordIndex = wordIndex(bitIndex);
    if (wordIndex >= wordsInUse)
      return;

    words[wordIndex] &= ~(1L << bitIndex);

    recalculateWordsInUse();
    checkInvariants();
  }

  /**
   * Sets the bits from the specified <tt>fromIndex</tt> (inclusive) to the
   * specified <tt>toIndex</tt> (exclusive) to <code>false</code>.
   *
   * @param fromIndex index of the first bit to be cleared.
   * @param toIndex   index after the last bit to be cleared.
   * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
   *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
   *                                   larger than <tt>toIndex</tt>.
   * @since 1.4
   */
  public void clear(final int fromIndex, int toIndex) {
    checkRange(fromIndex, toIndex);

    if (fromIndex == toIndex)
      return;

    final int startWordIndex = wordIndex(fromIndex);
    if (startWordIndex >= wordsInUse)
      return;

    int endWordIndex = wordIndex(toIndex - 1);
    if (endWordIndex >= wordsInUse) {
      toIndex = length();
      endWordIndex = wordsInUse - 1;
    }

    final long firstWordMask = WORD_MASK << fromIndex;
    final long lastWordMask = WORD_MASK >>> -toIndex;
    if (startWordIndex == endWordIndex) {
      // Case 1: One word
      words[startWordIndex] &= ~(firstWordMask & lastWordMask);
    } else {
      // Case 2: Multiple words
      // Handle first word
      words[startWordIndex] &= ~firstWordMask;

      // Handle intermediate words, if any
      for (int i = startWordIndex + 1; i < endWordIndex; i++)
        words[i] = 0;

      // Handle last word
      words[endWordIndex] &= ~lastWordMask;
    }

    recalculateWordsInUse();
    checkInvariants();
  }

  /**
   * Sets all of the bits in this BitSet to <code>false</code>.
   *
   * @since 1.4
   */
  public void clear() {
    while (wordsInUse > 0)
      words[--wordsInUse] = 0;
  }

  /**
   * Returns the value of the bit with the specified index. The value
   * is <code>true</code> if the bit with the index <code>bitIndex</code>
   * is currently set in this <code>BitSet</code>; otherwise, the result
   * is <code>false</code>.
   *
   * @param bitIndex the bit index.
   * @return the value of the bit with the specified index.
   * @throws IndexOutOfBoundsException if the specified index is negative.
   */
  public boolean get(final int bitIndex) {
    if (bitIndex < 0)
      throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

    checkInvariants();

    final int wordIndex = wordIndex(bitIndex);
    return (wordIndex < wordsInUse)
      && ((words[wordIndex] & (1L << bitIndex)) != 0);
  }

  /**
   * Returns a new <tt>BitSet</tt> composed of bits from this <tt>BitSet</tt>
   * from <tt>fromIndex</tt> (inclusive) to <tt>toIndex</tt> (exclusive).
   *
   * @param fromIndex index of the first bit to include.
   * @param toIndex   index after the last bit to include.
   * @return a new <tt>BitSet</tt> from a range of this <tt>BitSet</tt>.
   * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
   *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
   *                                   larger than <tt>toIndex</tt>.
   * @since 1.4
   */
  public RapidBitSet get(final int fromIndex, int toIndex) {
    checkRange(fromIndex, toIndex);

    checkInvariants();

    final int len = length();

    // If no set bits in range return empty bitset
    if (len <= fromIndex || fromIndex == toIndex)
      return new RapidBitSet(0);

    // An optimization
    if (toIndex > len)
      toIndex = len;

    final RapidBitSet result = new RapidBitSet(toIndex - fromIndex);
    final int targetWords = wordIndex(toIndex - fromIndex - 1) + 1;
    int sourceIndex = wordIndex(fromIndex);
    final boolean wordAligned = ((fromIndex & BIT_INDEX_MASK) == 0);

    // Process all words but the last word
    for (int i = 0; i < targetWords - 1; i++, sourceIndex++)
      result.words[i] = wordAligned ? words[sourceIndex] :
        (words[sourceIndex] >>> fromIndex) |
          (words[sourceIndex + 1] << -fromIndex);

    // Process the last word
    final long lastWordMask = WORD_MASK >>> -toIndex;
    result.words[targetWords - 1] =
      ((toIndex - 1) & BIT_INDEX_MASK) < (fromIndex & BIT_INDEX_MASK)
        ? /* straddles source words */
        ((words[sourceIndex] >>> fromIndex) |
          (words[sourceIndex + 1] & lastWordMask) << -fromIndex)
        :
        ((words[sourceIndex] & lastWordMask) >>> fromIndex);

    // Set wordsInUse correctly
    result.wordsInUse = targetWords;
    result.recalculateWordsInUse();
    result.checkInvariants();

    return result;
  }

  /**
   * Returns the index of the first bit that is set to <code>true</code>
   * that occurs on or after the specified starting index. If no such
   * bit exists then -1 is returned.
   * <p/>
   * To iterate over the <code>true</code> bits in a <code>BitSet</code>,
   * use the following loop:
   * <p/>
   * <pre>
   * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
   *     // operate on index i here
   * }</pre>
   *
   * @param fromIndex the index to start checking from (inclusive).
   * @return the index of the next set bit.
   * @throws IndexOutOfBoundsException if the specified index is negative.
   * @since 1.4
   */
  public int nextSetBit(final int fromIndex) {
    if (fromIndex < 0)
      throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

    checkInvariants();

    int u = wordIndex(fromIndex);
    if (u >= wordsInUse)
      return -1;

    long word = words[u] & (WORD_MASK << fromIndex);

    while (true) {
      if (word != 0)
        return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
      if (++u == wordsInUse)
        return -1;
      word = words[u];
    }
  }

  public int nextSetBitAnd(final RapidBitSet other, final int fromIndex) {
    if (fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

    final int minWordsInUse = Math.min(wordsInUse, other.wordsInUse);

    int u = wordIndex(fromIndex);
    if (u >= minWordsInUse) return -1;

    final long mask = WORD_MASK << fromIndex;

    final long[] ws = words;
    final long[] ows = other.words;

    long word = ws[u] & mask;
    long oword = ows[u] & mask;

    int offset = u * BITS_PER_WORD;

    while (true) {
      final long and = word & oword;
      if (and != 0) {
        return offset + Long.numberOfTrailingZeros(and);
      }
      if (++u == minWordsInUse) return -1;
      offset = u * BITS_PER_WORD;
      word = ws[u];
      oword = ows[u];
    }
  }

  /**
   * Returns the index of the first bit that is set to <code>false</code>
   * that occurs on or after the specified starting index.
   *
   * @param fromIndex the index to start checking from (inclusive).
   * @return the index of the next clear bit.
   * @throws IndexOutOfBoundsException if the specified index is negative.
   * @since 1.4
   */
  public int nextClearBit(final int fromIndex) {
    // Neither spec nor implementation handle bitsets of maximal length.
    // See 4816253.
    if (fromIndex < 0)
      throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

    checkInvariants();

    int u = wordIndex(fromIndex);
    if (u >= wordsInUse)
      return fromIndex;

    long word = ~words[u] & (WORD_MASK << fromIndex);

    while (true) {
      if (word != 0)
        return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
      if (++u == wordsInUse)
        return wordsInUse * BITS_PER_WORD;
      word = ~words[u];
    }
  }

  /**
   * Returns the "logical size" of this <code>BitSet</code>: the index of
   * the highest set bit in the <code>BitSet</code> plus one. Returns zero
   * if the <code>BitSet</code> contains no set bits.
   *
   * @return the logical size of this <code>BitSet</code>.
   * @since 1.2
   */
  public int length() {
    if (wordsInUse == 0)
      return 0;

    return BITS_PER_WORD * (wordsInUse - 1) +
      (BITS_PER_WORD - Long.numberOfLeadingZeros(words[wordsInUse - 1]));
  }

  /**
   * Returns true if this <code>BitSet</code> contains no bits that are set
   * to <code>true</code>.
   *
   * @return boolean indicating whether this <code>BitSet</code> is empty.
   * @since 1.4
   */
  public boolean isEmpty() {
    return wordsInUse == 0;
  }

  /**
   * Returns true if the specified <code>BitSet</code> has any bits set to
   * <code>true</code> that are also set to <code>true</code> in this
   * <code>BitSet</code>.
   *
   * @return boolean indicating whether this <code>BitSet</code> intersects
   *         the specified <code>BitSet</code>.
   * @param  set <code>BitSet</code> to intersect with
   * @since 1.4
   */
  public boolean intersects(final RapidBitSet set) {
    for (int i = Math.min(wordsInUse, set.wordsInUse) - 1; i >= 0; i--)
      if ((words[i] & set.words[i]) != 0)
        return true;
    return false;
  }

  public boolean intersectsFast(final RapidBitSet set) {
    final int first = Math.max(firstNonZero, set.firstNonZero);
    final int last = Math.min(lastNonZero, set.lastNonZero);

    if (first > last) return false;

    final long[] thisWords = words;
    final long[] otherWords = set.words;
    
    for (int i = last; i >= first; i--) {
      if ((thisWords[i] & otherWords[i]) != 0) return true;
    }
    return false;
  }

  /**
   * Returns the number of bits set to <tt>true</tt> in this
   * <code>BitSet</code>.
   *
   * @return the number of bits set to <tt>true</tt> in this
   *         <code>BitSet</code>.
   * @since 1.4
   */
  public int cardinality() {
    int sum = 0;
    final long[] words = this.words;
    for (int i = wordsInUse - 1; i >= 0 ; i--) {
      final long word = words[i];
      if (word != 0) {
        sum += Long.bitCount(word);
      }
    }
    return sum;
  }

  /**
   * Performs a logical <b>AND</b> of this target bit set with the
   * argument bit set. This bit set is modified so that each bit in it
   * has the value <code>true</code> if and only if it both initially
   * had the value <code>true</code> and the corresponding bit in the
   * bit set argument also had the value <code>true</code>.
   *
   * @param set a bit set.
   */
  public void and(final RapidBitSet set) {
    if (this == set)
      return;

    final long[] wC = words;
    while (wordsInUse > set.wordsInUse)
      wC[--wordsInUse] = 0;

    // Perform logical AND on words in common
    final long[] wC2 = set.words;
    for (int i = wordsInUse - 1; i >= 0; i--) {
      wC[i] &= wC2[i];
    }

    recalculateWordsInUse();
//    checkInvariants();
  }

  /**
   * Performs a logical <b>OR</b> of this bit set with the bit set
   * argument. This bit set is modified so that a bit in it has the
   * value <code>true</code> if and only if it either already had the
   * value <code>true</code> or the corresponding bit in the bit set
   * argument has the value <code>true</code>.
   *
   * @param set a bit set.
   */
  public void or(final RapidBitSet set) {
    if (this == set)
      return;

    final int wordsInCommon = Math.min(wordsInUse, set.wordsInUse);

    if (wordsInUse < set.wordsInUse) {
      ensureCapacity(set.wordsInUse);
      wordsInUse = set.wordsInUse;
    }

    // Perform logical OR on words in common
    for (int i = 0; i < wordsInCommon; i++)
      words[i] |= set.words[i];

    // Copy any remaining words
    if (wordsInCommon < set.wordsInUse)
      System.arraycopy(set.words, wordsInCommon,
        words, wordsInCommon,
        wordsInUse - wordsInCommon);

    // recalculateWordsInUse() is unnecessary
    checkInvariants();
  }

  /**
   * Performs a logical <b>XOR</b> of this bit set with the bit set
   * argument. This bit set is modified so that a bit in it has the
   * value <code>true</code> if and only if one of the following
   * statements holds:
   * <ul>
   * <li>The bit initially has the value <code>true</code>, and the
   * corresponding bit in the argument has the value <code>false</code>.
   * <li>The bit initially has the value <code>false</code>, and the
   * corresponding bit in the argument has the value <code>true</code>.
   * </ul>
   *
   * @param set a bit set.
   */
  public void xor(final RapidBitSet set) {
    final int wordsInCommon = Math.min(wordsInUse, set.wordsInUse);

    if (wordsInUse < set.wordsInUse) {
      ensureCapacity(set.wordsInUse);
      wordsInUse = set.wordsInUse;
    }

    // Perform logical XOR on words in common
    for (int i = 0; i < wordsInCommon; i++)
      words[i] ^= set.words[i];

    // Copy any remaining words
    if (wordsInCommon < set.wordsInUse)
      System.arraycopy(set.words, wordsInCommon,
        words, wordsInCommon,
        set.wordsInUse - wordsInCommon);

    recalculateWordsInUse();
    checkInvariants();
  }

  /**
   * Clears all of the bits in this <code>BitSet</code> whose corresponding
   * bit is set in the specified <code>BitSet</code>.
   *
   * @param set the <code>BitSet</code> with which to mask this
   *            <code>BitSet</code>.
   * @since 1.2
   */
  public void andNot(final RapidBitSet set) {
    // Perform logical (a & !b) on words in common
    for (int i = Math.min(wordsInUse, set.wordsInUse) - 1; i >= 0; i--)
      words[i] &= ~set.words[i];

    recalculateWordsInUse();
    checkInvariants();
  }

  /**
   * Returns a hash code value for this bit set. The hash code
   * depends only on which bits have been set within this
   * <code>BitSet</code>. The algorithm used to compute it may
   * be described as follows.<p>
   * Suppose the bits in the <code>BitSet</code> were to be stored
   * in an array of <code>long</code> integers called, say,
   * <code>words</code>, in such a manner that bit <code>k</code> is
   * set in the <code>BitSet</code> (for nonnegative values of
   * <code>k</code>) if and only if the expression
   * <pre>((k&gt;&gt;6) &lt; words.length) && ((words[k&gt;&gt;6] & (1L &lt;&lt; (bit & 0x3F))) != 0)</pre>
   * is true. Then the following definition of the <code>hashCode</code>
   * method would be a correct implementation of the actual algorithm:
   * <pre>
   * public int hashCode() {
   *      long h = 1234;
   *      for (int i = words.length; --i &gt;= 0; ) {
   *           h ^= words[i] * (i + 1);
   *      }
   *      return (int)((h &gt;&gt; 32) ^ h);
   * }</pre>
   * Note that the hash code values change if the set of bits is altered.
   * <p>Overrides the <code>hashCode</code> method of <code>Object</code>.
   *
   * @return a hash code value for this bit set.
   */
  public int hashCode() {
    long h = 1234;
    for (int i = wordsInUse; --i >= 0;)
      h ^= words[i] * (i + 1);

    return (int) ((h >> 32) ^ h);
  }

  /**
   * Returns the number of bits of space actually in use by this
   * <code>BitSet</code> to represent bit values.
   * The maximum element in the set is the size - 1st element.
   *
   * @return the number of bits currently in this bit set.
   */
  public int size() {
    return words.length * BITS_PER_WORD;
  }

  /**
   * Compares this object against the specified object.
   * The result is <code>true</code> if and only if the argument is
   * not <code>null</code> and is a <code>Bitset</code> object that has
   * exactly the same set of bits set to <code>true</code> as this bit
   * set. That is, for every nonnegative <code>int</code> index <code>k</code>,
   * <pre>((BitSet)obj).at(k) == this.at(k)</pre>
   * must be true. The current sizes of the two bit sets are not compared.
   * <p>Overrides the <code>equals</code> method of <code>Object</code>.
   *
   * @param obj the object to compare with.
   * @return <code>true</code> if the objects are the same;
   *         <code>false</code> otherwise.
   * @see java.util.BitSet#size()
   */
  public boolean equals(final Object obj) {
    if (!(obj instanceof RapidBitSet))
      return false;
    if (this == obj)
      return true;

    final RapidBitSet set = (RapidBitSet) obj;

    checkInvariants();
    set.checkInvariants();

    if (wordsInUse != set.wordsInUse)
      return false;

    // Check words in use by both BitSets
    for (int i = 0; i < wordsInUse; i++)
      if (words[i] != set.words[i])
        return false;

    return true;
  }

  /**
   * Cloning this <code>BitSet</code> produces a new <code>BitSet</code>
   * that is equal to it.
   * The clone of the bit set is another bit set that has exactly the
   * same bits set to <code>true</code> as this bit set.
   * <p/>
   * <p>Overrides the <code>clone</code> method of <code>Object</code>.
   *
   * @return a clone of this bit set.
   * @see java.util.BitSet#size()
   */
  @Override
  public Object clone() {
    if (!sizeIsSticky)
      trimToSize();

    try {
      final RapidBitSet result = (RapidBitSet) super.clone();
      result.words = words.clone();
      result.checkInvariants();
      return result;
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }

  public RapidBitSet duplicate() {
    if (!sizeIsSticky)
      trimToSize();

    final RapidBitSet result = new RapidBitSet();
    final int length = words.length;
    result.words = new long[length];
    System.arraycopy(words, 0, result.words, 0, length);
    result.checkInvariants();
    return result;
  }

  public void copyTo(final RapidBitSet other) {
    if (!sizeIsSticky) trimToSize();
    final int length = words.length;
    if (other.words.length < length) other.words = new long[length * 2];
    System.arraycopy(words, 0, other.words, 0, length);
    other.wordsInUse = wordsInUse;
    other.sizeIsSticky = sizeIsSticky;
//    other.checkInvariants();
  }

  /**
   * Attempts to reduce internal storage used for the bits in this bit set.
   * Calling this method may, but is not required to, affect the value
   * returned by a subsequent call to the {@link #size()} method.
   */
  private void trimToSize() {
    if (wordsInUse != words.length) {
      words = Arrays.copyOf(words, wordsInUse);
      checkInvariants();
    }
  }

  /**
   * Save the state of the <tt>BitSet</tt> instance to a stream (i.e.,
   * serialize it).
   */
  private void writeObject(final ObjectOutputStream s)
    throws IOException {

    checkInvariants();

    if (!sizeIsSticky)
      trimToSize();

    final ObjectOutputStream.PutField fields = s.putFields();
    fields.put("bits", words);
    s.writeFields();
  }

  /**
   * Reconstitute the <tt>BitSet</tt> instance from a stream (i.e.,
   * deserialize it).
   */
  private void readObject(final ObjectInputStream s)
    throws IOException, ClassNotFoundException {

    final ObjectInputStream.GetField fields = s.readFields();
    words = (long[]) fields.get("bits", null);

    // Assume maximum length then find real length
    // because recalculateWordsInUse assumes maintenance
    // or reduction in logical size
    wordsInUse = words.length;
    recalculateWordsInUse();
    sizeIsSticky = (words.length > 0 && words[words.length - 1] == 0L); // heuristic
    checkInvariants();
  }

  /**
   * Returns a string representation of this bit set. For every index
   * for which this <code>BitSet</code> contains a bit in the set
   * state, the decimal representation of that index is included in
   * the result. Such indices are listed in order from lowest to
   * highest, separated by ",&nbsp;" (a comma and a space) and
   * surrounded by braces, resulting in the usual mathematical
   * notation for a set of integers.<p>
   * Overrides the <code>toString</code> method of <code>Object</code>.
   * <p>Example:
   * <pre>
   * BitSet drPepper = new BitSet();</pre>
   * Now <code>drPepper.toString()</code> returns "<code>{}</code>".<p>
   * <pre>
   * drPepper.set(2);</pre>
   * Now <code>drPepper.toString()</code> returns "<code>{2}</code>".<p>
   * <pre>
   * drPepper.set(4);
   * drPepper.set(10);</pre>
   * Now <code>drPepper.toString()</code> returns "<code>{2, 4, 10}</code>".
   *
   * @return a string representation of this bit set.
   */
  public String toString() {
    checkInvariants();

    final int numBits = (wordsInUse > 128) ?
      cardinality() : wordsInUse * BITS_PER_WORD;
    final StringBuilder b = new StringBuilder(6 * numBits + 2);
    b.append('{');

    int i = nextSetBit(0);
    if (i != -1) {
      b.append(i);
      for (i = nextSetBit(i + 1); i >= 0; i = nextSetBit(i + 1)) {
        final int endOfRun = nextClearBit(i);
        do {
          b.append(", ").append(i);
        }
        while (++i < endOfRun);
      }
    }

    b.append('}');
    return b.toString();
  }
}
