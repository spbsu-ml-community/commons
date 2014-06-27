package com.spbsu.commons.text.charset.bigram;

import junit.framework.TestCase;

/**
 * @author lyadzhin
 */
public class CharBigramTest extends TestCase {
  public void testComparison() {
    final CharBigram bigram1 = CharBigram.valueOf("be");
    final CharBigram bigram2 = CharBigram.valueOf("ba");
    final CharBigram bigram3 = CharBigram.valueOf("ye");
    final CharBigram bigram4 = CharBigram.valueOf("ye");
    assertTrue(bigram1.compareTo(bigram2) > 0);
    assertTrue(bigram2.compareTo(bigram1) < 0);
    assertTrue(bigram1.compareTo(bigram3) < 0);
    assertTrue(bigram2.compareTo(bigram3) < 0);
    assertTrue(bigram3.compareTo(bigram4) == 0);
  }
}
