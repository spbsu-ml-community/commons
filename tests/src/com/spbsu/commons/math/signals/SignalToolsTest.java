package com.spbsu.commons.math.signals;

import com.spbsu.commons.math.signals.generic.GenericSignal;
import com.spbsu.commons.math.signals.generic.HashSetAdditiveSignal;
import com.spbsu.commons.math.signals.generic.SetAdditiveSignal;
import com.spbsu.commons.math.signals.tools.SignalTools;
import com.spbsu.commons.math.stat.Distribution;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Set;

/**
 * @author vp
 */
public class SignalToolsTest extends TestCase {
  public void testSumGenericSignals() throws Exception {
    final GenericSignal<String> a = new GenericSignal<String>();
    a.occur(100, "a");
    a.occur(200, "b");
    a.occur(300, "c");

    final GenericSignal<String> b = new GenericSignal<String>();
    b.occur(50, "qqq");
    b.occur(200, "a");
    b.occur(300, "c");

    final GenericSignal<Set<String>> sum = SignalTools.sumGenericSignals(a, b);
    assertEquals(4, sum.getTimestamps().length);
    assertTrue(Arrays.equals(new long[]{50, 100, 200, 300}, sum.getTimestamps()));
    sum.process(new SignalProcessor<Set<String>>() {
      @Override
      public void process(final long timestamp, final Set<String> value) {
        if (timestamp == 50) assertTrue(value.contains("qqq"));
        if (timestamp == 100) assertTrue(value.contains("a"));
        if (timestamp == 200) {
          assertEquals(2, value.size());
          assertTrue(value.contains("a"));
          assertTrue(value.contains("b"));
        }
        if (timestamp == 300) {
          assertEquals(1, value.size());
          assertTrue(value.contains("c"));
        }
      }
    });
  }


  public void testDistributionWithCollection() throws Exception {
    final SetAdditiveSignal<String> additiveSignal = new HashSetAdditiveSignal<String>();
    additiveSignal.occur(100, "a");
    additiveSignal.occur(200, "b");

    additiveSignal.occur(300, "c");
    additiveSignal.occur(300, "a");
    additiveSignal.occur(300, "a");
    additiveSignal.occur(300, "d");
    additiveSignal.occur(300, "b");
    additiveSignal.occur(300, "c");

    final Distribution<String> distribution = SignalTools.getValueDistributionWithCollection(additiveSignal.getSignal());
    assertEquals(2d / 6, distribution.getProbability("a"));
    assertEquals(2d / 6, distribution.getProbability("b"));
    assertEquals(1d / 6, distribution.getProbability("c"));
    assertEquals(1d / 6, distribution.getProbability("d"));
  }

}
