package com.spbsu.commons.text;

import com.spbsu.commons.seq.CharSeqTools;
import junit.framework.TestCase;

/**
 * User: starlight
 * Date: 15.04.14
 */
public class CharSeqToolsTest extends TestCase {
  public void testCutBetween() {
    assertEquals("abc", CharSeqTools.cutBetween("{abc}", 0, '{', '}'));
    assertEquals("abc", CharSeqTools.cutBetween("abc{abc}", 0, '{', '}'));
    assertEquals("abc", CharSeqTools.cutBetween("abc{abc}ab", 0, '{', '}'));
    assertEquals("abc", CharSeqTools.cutBetween("abc{abc", 0, '{', '}'));
    assertEquals("abc", CharSeqTools.cutBetween("abc{abc{abc}a}ab", 5, '{', '}'));
    assertEquals("abc{abc}a", CharSeqTools.cutBetween("abc{abc{abc}a}ab", 0, '{', '}'));
    assertEquals("", CharSeqTools.cutBetween("abc", 0, '{', '}'));
    assertEquals("", CharSeqTools.cutBetween("abc{abc}ab", 5, '{', '}'));
  }
}