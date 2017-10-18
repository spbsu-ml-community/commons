package com.expleague.commons.util;

import com.expleague.commons.util.frame.Frame;
import com.expleague.commons.util.frame.FrameTools;
import junit.framework.TestCase;

/**
 * User: terry
 * Date: 31.10.2009
 * Time: 14:18:05
 */
public class FrameTest extends TestCase {

  public void testContains() {
    final Frame<Integer> first = Frame.create(1, 8);
    assertTrue(first.contains(3));
    assertFalse(first.contains(0));
    final Frame<Integer> second = Frame.create(1, 7);
    final Frame<Integer> third = Frame.create(2, 7);
    final Frame<Integer> forth = Frame.create(1, 9);
    final Frame<Integer> fifth = Frame.create(-1, 7);
    assertTrue(FrameTools.contains(first, second));
    assertTrue(FrameTools.contains(first, third));
    assertFalse(FrameTools.contains(first, forth));
    assertFalse(FrameTools.contains(first, fifth));
  }

  public void testMerge() {
    final Frame<Integer> zero = Frame.create(8, 10);
    final Frame<Integer> first = Frame.create(1, 8);
    final Frame<Integer> second = Frame.create(1, 7);
    final Frame<Integer> third = Frame.create(2, 7);
    final Frame<Integer> forth = Frame.create(1, 9);
    final Frame<Integer> fifth = Frame.create(-1, 7);

    final Frame<Integer> six = Frame.create(10, 12);

    Frame<Integer>[] frames = FrameTools.merge(first, second);
    assertEquals(1, frames.length);
    assertEquals(1, frames[0].getStart().intValue());
    assertEquals(8, frames[0].getEnd().intValue());

    frames = FrameTools.merge(first, second, third, forth, fifth);
    assertEquals(1, frames.length);
    assertEquals(-1, frames[0].getStart().intValue());
    assertEquals(9, frames[0].getEnd().intValue());

    frames = FrameTools.merge(first, six);
    assertEquals(2, frames.length);
    assertEquals(first, frames[0]);
    assertEquals(six, frames[1]);

    frames = FrameTools.merge(first, zero);
    assertEquals(1, frames.length);
    assertEquals(1, frames[0].getStart().intValue());
    assertEquals(10, frames[0].getEnd().intValue());
  }
}
