package com.spbsu.commons.text.charset.bigram;

import junit.framework.TestCase;

/**
 * @author lyadzhin
 */
public class BigramsTextAnalyzerTest extends TestCase {
  private BigramsTextAnalyzer textAnalyzer;

  public void setUp() throws Exception {
    super.setUp();
    textAnalyzer = new BigramsTextAnalyzer();
    textAnalyzer.setCharFilter(CharFilter.ACCEPT_ALL_FILTER);
    textAnalyzer.setIgnoreCase(false);
  }

  public void testMolokolomol() {
    final BigramsTable result = textAnalyzer.buildBigramsTable("molokolomol");
    assertTrue(result.containsBigram(CharBigram.valueOf("mo")));
    assertTrue(result.containsBigram(CharBigram.valueOf("ol")));
    assertTrue(result.containsBigram(CharBigram.valueOf("lo")));
    assertTrue(result.containsBigram(CharBigram.valueOf("ok")));
    assertTrue(result.containsBigram(CharBigram.valueOf("ko")));
    assertTrue(result.containsBigram(CharBigram.valueOf("om")));
    final int size = "molokolomol".length() - 1;
    assertEquals(2. / size, result.getFrequency(CharBigram.valueOf("mo")));
    assertEquals(3. / size, result.getFrequency(CharBigram.valueOf("ol")));
    assertEquals(2. / size, result.getFrequency(CharBigram.valueOf("lo")));
    assertEquals(1. / size, result.getFrequency(CharBigram.valueOf("ok")));
    assertEquals(1. / size, result.getFrequency(CharBigram.valueOf("ko")));
    assertEquals(1. / size, result.getFrequency(CharBigram.valueOf("om")));
  }
}
