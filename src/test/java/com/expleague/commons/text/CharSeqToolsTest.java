package com.expleague.commons.text;

import com.expleague.commons.JUnitIOCapture;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.seq.*;
import com.expleague.commons.util.logging.Interval;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * User: starlight
 * Date: 15.04.14
 */
public class CharSeqToolsTest extends JUnitIOCapture {
  @Test
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

  @Test
  public void testHexToCharByte() {
    assertEquals(0, CharSeqTools.hexCharToByte('0'));
    assertEquals(9, CharSeqTools.hexCharToByte('9'));
    assertEquals(10, CharSeqTools.hexCharToByte('a'));
    assertEquals(10, CharSeqTools.hexCharToByte('A'));
    assertEquals(15, CharSeqTools.hexCharToByte('f'));
    assertEquals(15, CharSeqTools.hexCharToByte('F'));
  }

  @Test
  public void testCountLeadingOccurrences() {
    assertEquals(0, CharSeqTools.countLeadingOccurrences("abcd", '/'));
    assertEquals(0, CharSeqTools.countLeadingOccurrences("ab/cd/", '/'));
    assertEquals(1, CharSeqTools.countLeadingOccurrences("/abcd", '/'));
    assertEquals(5, CharSeqTools.countLeadingOccurrences("/////abcd/", '/'));
    assertEquals(5, CharSeqTools.countLeadingOccurrences("/////", '/'));
  }

  @Test
  public void testRemoveLeading() {
    assertEquals("abcd", CharSeqTools.removeLeading("abcd", '/'));
    assertEquals("ab/cd/", CharSeqTools.removeLeading("ab/cd/", '/'));
    assertEquals("abcd", CharSeqTools.removeLeading("/abcd", '/'));
    assertEquals("abcd/", CharSeqTools.removeLeading("/////abcd/", '/'));
    assertEquals("", CharSeqTools.removeLeading("/////", '/'));
  }

  @Test
  public void testClosemostString() {
    assertEquals(CharSeq.create("3"), CharSeqTools.closestSubstring("3", "8LEDs Car Truck Police Strobe Flash Light Dash Emergency 3 Flashing Mode Lights"));
    assertEquals(CharSeq.create("ABC"), CharSeqTools.closestSubstring("abc", "askhjd asdfiqrasdfjhgABCsjdhk"));
    assertEquals(CharSeq.create("aSbc"), CharSeqTools.closestSubstring("abc", "askhjd asdfiqrasdfjhgaSbcsjdhk"));
    assertEquals(CharSeq.create("Men\\'s"), CharSeqTools.closestSubstring("mens", "Men\\'s O-Neck Long Sleeve Striped Slim Fit Pullover Casual Sweater"));
  }

  @Test
  public void testChopperPerformance() throws IOException {
    final FastRandom rng = new FastRandom();
    Interval.start();
    for (int i = 0; i < 100_000; i++) {
      final ReaderChopper chopper = new ReaderChopper(rng.base64Stream(100));
      //noinspection StatementWithEmptyBody
      while(chopper.chop('a', 'b', 'c') != null);
    }
    Interval.stopAndPrint();
  }
}
