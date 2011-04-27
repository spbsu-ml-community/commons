package com.spbsu.commons.util;

import gnu.trove.TIntHashSet;
import junit.framework.TestCase;

import java.util.Random;

/**
 * @author vp
 */
public class RapidBitSetTest extends TestCase {
  public void testNextSetBitAnd() throws Exception {
    final RapidBitSet s1 = new RapidBitSet();
    final RapidBitSet s2 = new RapidBitSet();

    s1.set(20);
    s1.set(30);
    s1.set(40);

    s2.set(10);
    s2.set(30);
    s2.set(50);

    assertEquals(30, s1.nextSetBitAnd(s2, 0));
  }

  public void testNextSetBitAnd2() throws Exception {
    final RapidBitSet s1 = new RapidBitSet();
    final RapidBitSet s2 = new RapidBitSet();

    s1.set(20);
    s1.set(30);
    s1.set(40);

    s2.set(10);
    s2.set(20);
    s2.set(30);

    assertEquals(20, s1.nextSetBitAnd(s2, 0));
    assertEquals(30, s1.nextSetBitAnd(s2, 21));
  }

  public void testNextSetBitAnd3() throws Exception {
    final RapidBitSet s1 = new RapidBitSet();
    final RapidBitSet s2 = new RapidBitSet();

    s1.set(1);
    s1.set(2);
    s1.set(3);

    s2.set(4);
    s2.set(5);
    s2.set(6);

    assertEquals(-1, s1.nextSetBitAnd(s2, 0));
  }

  public void testNextSetBitAnd4() throws Exception {
    final RapidBitSet s1 = new RapidBitSet();
    final RapidBitSet s2 = new RapidBitSet();

    s1.set(20);
    s1.set(30);
    s1.set(40);
    s1.set(1223);
    s1.set(8);
    s1.set(324);

    s2.set(10);
    s2.set(30);
    s2.set(50);
    s2.set(8);
    s2.set(124);

    assertEquals(8, s1.nextSetBitAnd(s2, 0));
    assertEquals(30, s1.nextSetBitAnd(s2, 9));
    assertEquals(-1, s1.nextSetBitAnd(s2, 31));
  }

  public void testNextSetBitAnd5() throws Exception {
    final RapidBitSet s1 = new RapidBitSet();
    final RapidBitSet s2 = new RapidBitSet();

    s1.set(2);
    s1.set(4);

    s2.set(4);

    assertEquals(4, s1.nextSetBitAnd(s2, 2));
  }

  public void testNextSetBitAnd6() throws Exception {
    final RapidBitSet s1 = new RapidBitSet();
    final RapidBitSet s2 = new RapidBitSet();

    final TIntHashSet mask = new TIntHashSet();

    final Random random = new Random();
    for (int i = 0; i < 5; i++) {
      int r = random.nextInt(100);
      while (mask.contains(r)) {
        r = random.nextInt(100);
      }
      mask.add(r);

      s1.set(100 + r);
      s2.set(100 + r);
    }
    for (int i = 0; i < 10; i++) {
      final int r = random.nextInt(100);
      s1.set(r);
      s2.set(200 + r);
    }

    final RapidBitSet set = new RapidBitSet();
    int bit = 0;
    while (true) {
      if ((bit = s1.nextSetBitAnd(s2, bit)) == -1) break;
      set.set(bit);
      bit++;
    }
    assertEquals(mask.size(), set.cardinality());
  }
}
