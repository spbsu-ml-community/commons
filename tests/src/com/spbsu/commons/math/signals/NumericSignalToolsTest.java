package com.spbsu.commons.math.signals;

import com.spbsu.commons.math.signals.numeric.BinarySignal;
import com.spbsu.commons.math.signals.numeric.IntSignal;
import com.spbsu.commons.math.signals.tools.NumericSignalTools;
import com.spbsu.commons.math.stat.NumericDistribution;
import com.spbsu.commons.math.stat.impl.FrameHistogram;
import com.spbsu.commons.util.logging.Interval;
import com.spbsu.commons.util.time.TimeFrame;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * @author vp
 */
public class NumericSignalToolsTest extends TestCase {

  public void testSumIntAndBinarySignals() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(100, 1);
    a.occur(200, 2);

    final IntSignal b = new IntSignal();
    b.occur(100, 1);
    b.occur(200, 1);
    b.occur(300, 2);
    final IntSignal sum = NumericSignalTools.sumIntArraySignals(a, b);
    assertEquals(3, sum.getTimestamps().length);
    assertTrue(Arrays.equals(new long[]{100, 200, 300}, sum.getTimestamps()));
     sum.process(new SignalProcessor<Integer>() {
      @Override
      public void process(final long timestamp, final Integer value) {
        if (timestamp == 100) assertEquals(2, value.intValue());
        if (timestamp == 200) assertEquals(3, value.intValue());
        if (timestamp == 300) assertEquals(2, value.intValue());
      }
    });
  }

  public void testSumIntSignals() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(100, -10);
    a.occur(200, 400);
    a.occur(300, 100);

    final IntSignal b = new IntSignal();
    b.occur(50, 500);
    b.occur(200, 70);
    b.occur(300, 1300);

    final IntSignal sum = NumericSignalTools.sumIntArraySignals(a, b);
    assertEquals(4, sum.getTimestamps().length);
    assertTrue(Arrays.equals(new long[]{50, 100, 200, 300}, sum.getTimestamps()));
    sum.process(new SignalProcessor<Integer>() {
      @Override
      public void process(final long timestamp, final Integer value) {
        if (timestamp == 50) assertEquals(500, value.intValue());
        if (timestamp == 100) assertEquals(-10, value.intValue());
        if (timestamp == 200) assertEquals(470, value.intValue());
        if (timestamp == 300) assertEquals(1400, value.intValue());
      }
    });
  }

  public void testDistribution() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(100, -10);
    a.occur(200, 400);
    a.occur(300, 100);
    a.occur(400, 100);

    final NumericDistribution<Integer> distribution = NumericSignalTools.getNumericValueDistribution(a);
    assertEquals(1d / 4, distribution.getProbability(-10));
    assertEquals(2d / 4, distribution.getProbability(100));
    assertEquals(1d / 4, distribution.getProbability(400));
    assertEquals(590d / 4, distribution.getMean());
    assertEquals(-10d, distribution.getMin());
    assertEquals(400d, distribution.getMax());
  }

  public void testDensityMacroAverage() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(0, 100);
    a.occur(200, 400);
    a.occur(300, 100);
    a.occur(400, 200);
    assertEquals(20., NumericSignalTools.densityMacroAverage(a, new TimeFrame(0L, 400L), 10));
    assertEquals(700/30., NumericSignalTools.densityMacroAverage(a, new TimeFrame(100L, 400L), 10));
    assertEquals(400/5., NumericSignalTools.densityMacroAverage(a, new TimeFrame(200L, 250L), 10));
  }

  public void testDensityMicroAverage() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(0, 100);
    a.occur(200, 400);
    a.occur(300, 100);
    a.occur(400, 200);
    assertEquals(
      NumericSignalTools.densityMacroAverage(a, new TimeFrame(0L, 400L), 10),
      NumericSignalTools.densityMicroAverage(a, new TimeFrame(0L, 400L), 400, 10, true)
    );
    final double microWithoutZeros = NumericSignalTools.densityMicroAverage(a, new TimeFrame(0L, 400L), 400, 10, true);
    assertEquals(
      NumericSignalTools.densityMacroAverage(a, new TimeFrame(0L, 400L), 10),
      NumericSignalTools.densityMicroAverage(a, new TimeFrame(0L, 400L), 400, 10, false)
    );
    final double microWithZeros = NumericSignalTools.densityMicroAverage(a, new TimeFrame(0L, 400L), 400, 10, false);
    assertEquals(
      (10 + 40 * 10 + 50 + 10 * 9 + 30) / 31.,
      NumericSignalTools.densityMicroAverage(a, new TimeFrame(0L, 400L), 100, 10, false)
    );
    assertEquals(
      (10 + 40 * 10 + 50 + 10 * 9 + 30) / (31. - 9),
      NumericSignalTools.densityMicroAverage(a, new TimeFrame(0L, 400L), 100, 10, true)
    );
  }

  public void testToFrame() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(0, 100);
    a.occur(200, 400);
    a.occur(300, 100);
    a.occur(400, 200);

    {
      final IntSignal signal = NumericSignalTools.toFrame(a, new TimeFrame(200L, 350L));
      final long[] timestamps = signal.getTimestamps();
      final int[] values = signal.getNativeValues();
      assertEquals(2, timestamps.length);
      assertEquals(200, timestamps[0]);
      assertEquals(300, timestamps[1]);
      assertEquals(400, values[0]);
      assertEquals(100, values[1]);
    }

    {
      final IntSignal signal = NumericSignalTools.toFrame(a, TimeFrame.INFINITY);
      assertEquals(a, signal);
    }

    {
      final IntSignal signal = NumericSignalTools.toFrame(a, new TimeFrame(-100, -50));
      assertEquals(0, signal.getTimestampCount());
    }

    {
      final IntSignal signal = NumericSignalTools.toFrame(a, new TimeFrame(1000, 2000));
      assertEquals(0, signal.getTimestampCount());
    }

    {
      final IntSignal empty = new IntSignal();
      final IntSignal signal = NumericSignalTools.toFrame(empty, new TimeFrame(1000, 2000));
      assertEquals(empty, signal);
    }
  }

  public void testToResolution() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(0, 10);
    a.occur(10, 20);
    a.occur(20, 30);
    a.occur(80, 40);
    a.occur(100, 400);
    a.occur(300, 100);
    a.occur(350, 50);

    final IntSignal signal = NumericSignalTools.toResolution(a, 100);
    final long[] timestamps = signal.getTimestamps();
    final int[] values = signal.getNativeValues();
    assertEquals(3, timestamps.length);
    assertEquals(0, timestamps[0]);
    assertEquals(100, timestamps[1]);
    assertEquals(300, timestamps[2]);
    assertEquals(100, values[0]);
    assertEquals(400, values[1]);
    assertEquals(150, values[2]);
  }

  public void testToResolution2() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(0, 10);
    a.occur(10, 20);
    a.occur(20, 30);
    a.occur(80, 40);
    a.occur(100, 400);
    a.occur(300, 100);
    a.occur(350, 50);

    final IntSignal signal = NumericSignalTools.toResolution(a, 1);
    assertTrue(Arrays.equals(a.getTimestamps(), signal.getTimestamps()));
    assertTrue(Arrays.equals(a.getNativeValues(), signal.getNativeValues()));
  }

  public void testSumValues() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(0, 10);
    a.occur(10, 20);
    a.occur(20, 30);
    a.occur(80, 40);
    a.occur(100, 400);
    a.occur(300, 100);
    a.occur(350, 50);

    assertEquals(10 + 20 + 30 + 40 + 400 + 100 + 50, NumericSignalTools.sumValues(a));
  }

  public void testToHistogramByBinResolution() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(0, 10);
    a.occur(10, 20);
    a.occur(20, 30);
    a.occur(80, 40);
    a.occur(100, 400);
    a.occur(300, 100);
    a.occur(350, 50);

    final FrameHistogram histogram = NumericSignalTools.toHistogramByResolution(a, 100);
    assertEquals(4, histogram.getBinCount());
    assertEquals(100, histogram.getResolution());
    assertEquals(100, histogram.getValue(0));
    assertEquals(400, histogram.getValue(1));
    assertEquals(0, histogram.getValue(2));
    assertEquals(150, histogram.getValue(3));
  }

  public void testToHistogramByBinCount() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(0, 10);
    a.occur(10, 20);
    a.occur(20, 30);
    a.occur(80, 40);
    a.occur(100, 400);
    a.occur(300, 100);
    a.occur(350, 50);

    final FrameHistogram histogram = NumericSignalTools.toHistogramByBinCount(a, 5);
    assertEquals(5, histogram.getBinCount());
    assertEquals(70, histogram.getResolution());
    assertEquals(60, histogram.getValue(0));
    assertEquals(440, histogram.getValue(1));
    assertEquals(0, histogram.getValue(2));
    assertEquals(0, histogram.getValue(3));
    assertEquals(150, histogram.getValue(4));
  }

  public void testBefore() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(10, 20);
    a.occur(20, 30);
    a.occur(80, 40);
    a.occur(100, 400);
    a.occur(300, 100);
    a.occur(350, 50);

    {
      final IntSignal before = NumericSignalTools.before(a, 100);
      assertEquals(3, before.getTimestampCount());
      assertTrue(Arrays.equals(new long[]{10, 20, 80}, before.getTimestamps()));
      assertTrue(Arrays.equals(new int[]{20, 30, 40}, before.getNativeValues()));
    }

    {
      final IntSignal before = NumericSignalTools.before(a, 5);
      assertEquals(0, before.getTimestampCount());
      assertTrue(before.getTimeFrame().isEmpty());
    }

    {
      final IntSignal before = NumericSignalTools.before(a, 500);
      assertEquals(a.getTimestampCount(), before.getTimestampCount());
      assertEquals(a.getTimeFrame(), before.getTimeFrame());
      assertTrue(Arrays.equals(a.getTimestamps(), before.getTimestamps()));
      assertTrue(Arrays.equals(a.getNativeValues(), before.getNativeValues()));
    }

    {
      final BinarySignal b = new BinarySignal();
      b.occur(10);
      b.occur(20);
      final IntSignal before = NumericSignalTools.before(b, 100);
    }
  }

  public void testAfter() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(10, 20);
    a.occur(20, 30);
    a.occur(80, 40);
    a.occur(100, 400);
    a.occur(300, 100);
    a.occur(350, 50);

    {
      final IntSignal after = NumericSignalTools.after(a, 100);
      assertEquals(2, after.getTimestampCount());
      assertTrue(Arrays.equals(new long[]{300, 350}, after.getTimestamps()));
      assertTrue(Arrays.equals(new int[]{100, 50}, after.getNativeValues()));
    }

    {
      final IntSignal after = NumericSignalTools.after(a, 500);
      assertEquals(0, after.getTimestampCount());
      assertTrue(after.getTimeFrame().isEmpty());
    }

    {
      final IntSignal after = NumericSignalTools.after(a, 5);
      assertEquals(a.getTimestampCount(), after.getTimestampCount());
      assertEquals(a.getTimeFrame(), after.getTimeFrame());
      assertTrue(Arrays.equals(a.getTimestamps(), after.getTimestamps()));
      assertTrue(Arrays.equals(a.getNativeValues(), after.getNativeValues()));
    }

    {
      final BinarySignal b = new BinarySignal();
      b.occur(10);
      b.occur(20);
      final IntSignal after = NumericSignalTools.after(b, 0);
    }
  }

  public void testLastTimestampBefore() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(10, 20);
    a.occur(20, 30);
    a.occur(80, 40);
    a.occur(100, 400);
    a.occur(300, 100);
    a.occur(350, 50);

    assertEquals(10, NumericSignalTools.lastTimestampBefore(a, 20, -1));
    assertEquals(10, NumericSignalTools.lastTimestampBefore(a, 11, -1));
    assertEquals(-1, NumericSignalTools.lastTimestampBefore(a, 5, -1));
    assertEquals(300, NumericSignalTools.lastTimestampBefore(a, 301, -1));

    assertEquals(NumericSignalTools.lastTimestampBefore(a, 20, -1), NumericSignalTools.lastTimestampBeforeFast(a, 20, -1));
    assertEquals(NumericSignalTools.lastTimestampBefore(a, 11, -1), NumericSignalTools.lastTimestampBeforeFast(a, 11, -1));
    assertEquals(NumericSignalTools.lastTimestampBefore(a, 5, -1), NumericSignalTools.lastTimestampBeforeFast(a, 5, -1));
    assertEquals(NumericSignalTools.lastTimestampBefore(a, 301, -1), NumericSignalTools.lastTimestampBeforeFast(a, 301, -1));
  }

  public void testFirstTimestampAfter() throws Exception {
    final IntSignal a = new IntSignal();
    a.occur(10, 20);
    a.occur(20, 30);
    a.occur(80, 40);
    a.occur(100, 400);
    a.occur(300, 100);
    a.occur(350, 50);

    assertEquals(80, NumericSignalTools.firstTimestampAfter(a, 20, -1));
    assertEquals(10, NumericSignalTools.firstTimestampAfter(a, 0, -1));
    assertEquals(-1, NumericSignalTools.firstTimestampAfter(a, 500, -1));
    assertEquals(300, NumericSignalTools.firstTimestampAfter(a, 299, -1));

    assertEquals(NumericSignalTools.firstTimestampAfter(a, 20, -1), NumericSignalTools.firstTimestampAfterFast(a, 20, -1));
    assertEquals(NumericSignalTools.firstTimestampAfter(a, 0, -1), NumericSignalTools.firstTimestampAfterFast(a, 0, -1));
    assertEquals(NumericSignalTools.firstTimestampAfter(a, 500, -1), NumericSignalTools.firstTimestampAfterFast(a, 500, -1));
    assertEquals(NumericSignalTools.firstTimestampAfter(a, 299, -1), NumericSignalTools.firstTimestampAfterFast(a, 299, -1));
  }

  public void testLastTimestampBeforePerformance() throws Exception {
    final IntSignal a = new IntSignal();
    final int count = 5000;
    for (int i = 0; i < count; i++) {
      a.occur(i * 10, i);
    }

    Interval.start();
    for (int i = 0; i < count; i++) {
      NumericSignalTools.lastTimestampBefore(a, i * 10, -1);
    }
    Interval.stopAndPrint();

    Interval.start();
    for (int i = 0; i < count; i++) {
      NumericSignalTools.lastTimestampBeforeFast(a, i * 10, -1);
    }
    Interval.stopAndPrint();
  }

  public void testTimestampCountInFrame() throws Exception {
    final IntSignal a = new IntSignal();
    final int count = 5000;
    for (int i = 0; i < count; i++) {
      a.occur(i * 10, i);
    }

    for (int i = -1000; i < count + 1000; i++) {
      final TimeFrame timeFrame = new TimeFrame(i * 10, i * 10 + 500);
      final IntSignal signal = NumericSignalTools.toFrame(a, timeFrame);
      assertEquals(signal.getTimestampCount(), NumericSignalTools.timestampCountInFrame(a, timeFrame));
    }
  }
}