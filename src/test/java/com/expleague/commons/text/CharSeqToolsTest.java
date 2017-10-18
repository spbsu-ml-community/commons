package com.expleague.commons.text;

import com.expleague.commons.seq.CharSeqTools;
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

  public void testHexToCharByte() {
    assertEquals(0, CharSeqTools.hexCharToByte('0'));
    assertEquals(9, CharSeqTools.hexCharToByte('9'));
    assertEquals(10, CharSeqTools.hexCharToByte('a'));
    assertEquals(10, CharSeqTools.hexCharToByte('A'));
    assertEquals(15, CharSeqTools.hexCharToByte('f'));
    assertEquals(15, CharSeqTools.hexCharToByte('F'));
  }

  public void testCountLeadingOccurrences() {
    assertEquals(0, CharSeqTools.countLeadingOccurrences("abcd", '/'));
    assertEquals(0, CharSeqTools.countLeadingOccurrences("ab/cd/", '/'));
    assertEquals(1, CharSeqTools.countLeadingOccurrences("/abcd", '/'));
    assertEquals(5, CharSeqTools.countLeadingOccurrences("/////abcd/", '/'));
    assertEquals(5, CharSeqTools.countLeadingOccurrences("/////", '/'));
  }

  public void testRemoveLeading() {
    assertEquals("abcd", CharSeqTools.removeLeading("abcd", '/'));
    assertEquals("ab/cd/", CharSeqTools.removeLeading("ab/cd/", '/'));
    assertEquals("abcd", CharSeqTools.removeLeading("/abcd", '/'));
    assertEquals("abcd/", CharSeqTools.removeLeading("/////abcd/", '/'));
    assertEquals("", CharSeqTools.removeLeading("/////", '/'));
  }
}
