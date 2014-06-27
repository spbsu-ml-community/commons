package com.spbsu.commons.math.signals;

import com.spbsu.commons.math.signals.generic.GenericSignal;
import com.spbsu.commons.util.Factories;

import gnu.trove.list.array.TLongArrayList;
import junit.framework.TestCase;

import java.util.*;

/**
 * @author vp
 */
public class GenericSignalTest extends TestCase {
  public void testIntegerSignalCreation() throws Exception {
    final long[] timestamps = {1, 2, 3, 4};
    final Integer[] values = {5, 6, 7, 8};
    final GenericSignal<Integer> s = new GenericSignal<Integer>(timestamps, values);
    assertEquals(timestamps.length, s.getTimestamps().length);
    assertTrue(Arrays.equals(timestamps, s.getTimestamps()));
    assertTrue(Arrays.equals(values, s.getValues()));
    final TLongArrayList timestampsTest = new TLongArrayList();
    final List<Integer> valuesTest = new ArrayList<Integer>();
    s.process(new SignalProcessor<Integer>() {
      @Override
      public void process(final long timestamp, final Integer value) {
        timestampsTest.add(timestamp);
        valuesTest.add(value);
      }
    });
    assertTrue(Arrays.equals(timestamps, timestampsTest.toArray()));
    assertTrue(Arrays.equals(values, valuesTest.toArray(new Integer[valuesTest.size()])));
  }

  public void testIntegerSignalSorting() throws Exception {
    final int length = 100;

    final GenericSignal<Integer> s = new GenericSignal<Integer>();
    final Random random = new Random();
    final Set<Integer> shuffled = Factories.hashSet();
    for (int i = 0; i < length; i++) {
      shuffled.add(random.nextInt(10000));
    }

    for (Integer timestamp : shuffled) {
      s.occur(timestamp, timestamp * timestamp);
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

  public void testGenericSignalGetValues() throws Exception {
    final GenericSignal<String> signal = new GenericSignal<String>();
    signal.occur(100, "s");
    final Object[] values = signal.getValues();
    assertEquals(1, values.length);
    assertEquals("s", values[0]);
  }

}
