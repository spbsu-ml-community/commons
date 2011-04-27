package com.spbsu.commons.math.signals;

import com.spbsu.commons.math.signals.numeric.BinarySignal;
import com.spbsu.commons.math.signals.numeric.IntSignal;
import com.spbsu.commons.math.signals.numeric.WrappingBinarySignal;
import com.spbsu.commons.util.Factories;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

/**
 * User: terry
 * Date: 12.12.2009
 */
public class NumericSignalTest extends TestCase {
  public void testAdd() throws Exception {
    final BinarySignal binarySignal = new BinarySignal();
    binarySignal.occur(10);
    binarySignal.occur(20);
    assertEquals(2, binarySignal.getTimestamps().length);
  }

  public void testAddTwiceTheSame() throws Exception {
    final BinarySignal binarySignal = new BinarySignal();
    binarySignal.occur(10);
    binarySignal.occur(10);
    assertEquals(1, binarySignal.getTimestamps().length);
  }

  public void testSignalSorting() throws Exception {
    final int length = 100;

    final IntSignal s = new IntSignal();
    s.occur(20, 1);
    s.occur(10, 2);
    s.occur(30, 3);
    s.occur(10, 4);
    s.occur(25, 6);
    final long[] timestamps = s.getTimestamps();
    assertEquals(4, timestamps.length);
    assertEquals(10, timestamps[0]);
    assertEquals(20, timestamps[1]);
    assertEquals(25, timestamps[2]);
    assertEquals(30, timestamps[3]);
    final int[] values = s.getNativeValues();
    assertEquals(6, values[0]);
    assertEquals(1, values[1]);
    assertEquals(6, values[2]);
    assertEquals(3, values[3]);
  }

  public void testSignalSortingRandom() throws Exception {
    final int length = 100;

    final BinarySignal s = new BinarySignal();
    final Random random = new Random();
    final Set<Integer> shuffled = Factories.hashSet();
    for (int i = 0; i < length; i++) {
      shuffled.add(random.nextInt(10000));
    }

    for (Integer timestamp : shuffled) {
      final int old = s.getTimestampCount();
      s.occur(timestamp);
      if (old == s.getTimestampCount()) {
        int a = 0;
        s.occur(timestamp);
      }
    }
    assertEquals(shuffled.size(), s.getTimestamps().length);

    s.process(new SignalProcessor<Integer>() {
      Long previous;

      @Override
      public void process(final long timestamp, final Integer value) {
        if (previous == null) previous = timestamp;
        else assertTrue(previous <= timestamp);
      }
    });
  }

  public void testFloor() throws Exception {
    final BinarySignal binarySignal = new BinarySignal();
    binarySignal.occur(10);
    binarySignal.occur(20);
    binarySignal.occur(30);

    assertEquals(-1, binarySignal.floor(0));
    assertEquals(10, binarySignal.floor(10));
    assertEquals(10, binarySignal.floor(15));
    assertEquals(20, binarySignal.floor(20));
    assertEquals(30, binarySignal.floor(100));
  }

  public void testFloorIndex() throws Exception {
    final BinarySignal binarySignal = new BinarySignal();
    binarySignal.occur(10);
    binarySignal.occur(20);
    binarySignal.occur(30);

    assertEquals(-1, binarySignal.floorIndex(0));
    assertEquals(0, binarySignal.floorIndex(10));
    assertEquals(0, binarySignal.floorIndex(15));
    assertEquals(1, binarySignal.floorIndex(20));
    assertEquals(2, binarySignal.floorIndex(100));
  }

  public void testCeil() throws Exception {
    final BinarySignal binarySignal = new BinarySignal();
    binarySignal.occur(10);
    binarySignal.occur(20);
    binarySignal.occur(30);

    assertEquals(10, binarySignal.ceil(0));
    assertEquals(10, binarySignal.ceil(10));
    assertEquals(20, binarySignal.ceil(15));
    assertEquals(20, binarySignal.ceil(20));
    assertEquals(-1, binarySignal.ceil(100));
  }

  public void testWrapper() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(10, 123);
    a.occur(20, 1);
    final WrappingBinarySignal w = new WrappingBinarySignal(a);
    assertEquals(a.getTimestampCount(), w.getTimestampCount());
    assertTrue(Arrays.equals(a.getTimestamps(), w.getTimestamps()));
    assertEquals(a.getTimestamp(0), w.getTimestamp(0));
    assertEquals(a.getTimestamp(1), w.getTimestamp(1));
  }
}
