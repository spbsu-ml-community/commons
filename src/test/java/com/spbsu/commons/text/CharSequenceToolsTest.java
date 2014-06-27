package com.spbsu.commons.text;

import junit.framework.TestCase;

/**
 * User: starlight
 * Date: 15.04.14
 */
public class CharSequenceToolsTest extends TestCase {
  public void testCutBetween() {
    assertEquals("abc", CharSequenceTools.cutBetween("{abc}", 0, '{', '}'));
    assertEquals("abc", CharSequenceTools.cutBetween("abc{abc}", 0, '{', '}'));
    assertEquals("abc", CharSequenceTools.cutBetween("abc{abc}ab", 0, '{', '}'));
    assertEquals("abc", CharSequenceTools.cutBetween("abc{abc", 0, '{', '}'));
    assertEquals("abc", CharSequenceTools.cutBetween("abc{abc{abc}a}ab", 5, '{', '}'));
    assertEquals("abc{abc}a", CharSequenceTools.cutBetween("abc{abc{abc}a}ab", 0, '{', '}'));
    assertEquals("", CharSequenceTools.cutBetween("abc", 0, '{', '}'));
    assertEquals("", CharSequenceTools.cutBetween("abc{abc}ab", 5, '{', '}'));
  }
}
