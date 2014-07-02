package com.spbsu.commons.math.signals.tools;

import com.spbsu.commons.math.signals.NumericSignal;
import com.spbsu.commons.math.signals.SignalProcessor;
import com.spbsu.commons.math.signals.numeric.BinarySignal;
import com.spbsu.commons.math.signals.numeric.IntSignal;
import com.spbsu.commons.math.stat.NumericDistribution;
import com.spbsu.commons.math.stat.impl.FrameHistogram;
import com.spbsu.commons.math.stat.impl.NumericSampleDistribution;
import com.spbsu.commons.util.frame.time.TimeFrame;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;


import java.util.ArrayList;

/**
 * @author vp
 */
public class NumericSignalTools {
  public static <T extends Number> NumericDistribution<T> getNumericValueDistribution(final NumericSignal<T> signal) {
    final NumericSampleDistribution<T> distribution = new NumericSampleDistribution<T>();
    signal.process(new SignalProcessor<T>() {
      @Override
      public void process(final long timestamp, final T value) {
        distribution.update(value);
      }
    });
    return distribution;
  }

  public static IntSignal sumIntArraySignals(final IntSignal... signals) {
    final IntSignal result = new IntSignal();
    for (IntSignal signal : signals) {
      if (signal instanceof BinarySignal) {
        for (int i = 0; i < signal.getTimestampCount(); i++) {
          result.occur(signal.getTimestamp(i), 1);
        }
      } else {
        for (int i = 0; i < signal.getTimestampCount(); i++) {
          result.occur(signal.getTimestamp(i), signal.getNativeValue(i));
        }
      }
    }
    return result;
  }

  public static IntSignal toFrame(final IntSignal signal, final TimeFrame frame) {
    if (frame == TimeFrame.INFINITY  || signal.getTimestampCount() == 0) return signal;

    if (signal instanceof BinarySignal) return toFrame((BinarySignal) signal, frame);

    final IntSignal result = new IntSignal();
    final int last = signal.floorIndex(frame.getEndTime());
    if (last == -1) return result;

    final int first = signal.ceilIndex(frame.getStartTime());
    if (first == -1) return result;

    for (int i = first; i <= last; i++) {
      result.occur(signal.getTimestamp(i), signal.getNativeValue(i));
    }
    return result;
  }

  public static BinarySignal toFrame(final BinarySignal signal, final TimeFrame frame) {
    if (frame == TimeFrame.INFINITY  || signal.getTimestampCount() == 0) return signal;

    final BinarySignal result = new BinarySignal();
    final int last = signal.floorIndex(frame.getEndTime());
    if (last == -1) return result;

    final int first = signal.ceilIndex(frame.getStartTime());
    if (first == -1) return result;

    for (int i = first; i <= last; i++) {
      result.occur(signal.getTimestamp(i));
    }
    return result;
  }

  public static IntSignal before(final IntSignal signal, final long timestamp) {
    return toFrame(signal, new TimeFrame(Long.MIN_VALUE, timestamp - 1));
  }

  public static IntSignal after(final IntSignal signal, final long timestamp) {
    return toFrame(signal, new TimeFrame(timestamp + 1, Long.MAX_VALUE));
  }

  public static IntSignal toResolution(final IntSignal signal, final long resolution) {
    final int count = signal.getTimestampCount();
    if (count == 0) return signal;

    if (signal instanceof BinarySignal) return toResolution((BinarySignal) signal, resolution);

    final TLongArrayList timestamps = new TLongArrayList();
    final TIntArrayList values = new TIntArrayList();
    long prev = signal.getTimestamp(0);
    int tmp = signal.getNativeValue(0);
    for (int i = 1; i < count; i++) {
      final long timestamp = signal.getTimestamp(i);
      final int value = signal.getNativeValue(i);
      if (timestamp - prev < resolution) {
        tmp += value;
        continue;
      }
      timestamps.add(prev);
      values.add(tmp);

      prev = timestamp;
      tmp = value;
    }
    if (prev != -1) {
      timestamps.add(prev);
      values.add(tmp);
    }
    return new IntSignal(timestamps.toArray(), values.toArray());
  }

  public static BinarySignal toResolution(final BinarySignal signal, final long resolution) {
    final int count = signal.getTimestampCount();
    if (count == 0) return signal;

    final TLongArrayList result = new TLongArrayList();
    long prev = signal.getTimestamp(0);
    for (int i = 1; i < count; i++) {
      final long timestamp = signal.getTimestamp(i);
      if (timestamp - prev < resolution) {
        continue;
      }
      result.add(prev);
      prev = timestamp;
    }
    if (prev != -1) result.add(prev);
    return new BinarySignal(result.toArray());
  }

  public static FrameHistogram toHistogramByResolution(final IntSignal signal, final long resolution) {
    return toHistogramByResolution(signal, signal.getTimeFrame(), resolution);
  }

  public static FrameHistogram toHistogramByResolution(final IntSignal signal, final TimeFrame frame, final long resolution) {
    return toHistogram(signal, frame, FrameHistogram.createByResolution(frame, resolution));
  }

  public static FrameHistogram toHistogramByBinCount(final IntSignal signal, final int binCount) {
    return toHistogramByBinCount(signal, signal.getTimeFrame(), binCount);
  }

  public static FrameHistogram toHistogramByBinCount(final IntSignal signal, final TimeFrame frame, final int binCount) {
    return toHistogram(signal, frame, FrameHistogram.createByBinCount(frame, binCount));
  }

  private static FrameHistogram toHistogram(final IntSignal signal, final TimeFrame frame, final FrameHistogram histogram) {
    if (signal.getTimestampCount() == 0) return histogram;

    final IntSignal inFrame = toFrame(signal, frame);
    final int timestampCount = inFrame.getTimestampCount();
    if (timestampCount == 0) return histogram;

    if (signal instanceof BinarySignal) {
      for (int i = 0; i < timestampCount; i++) {
        histogram.update(inFrame.getTimestamp(i), 1);
      }
    } else {
      for (int i = 0; i < timestampCount; i++) {
        histogram.update(inFrame.getTimestamp(i), inFrame.getNativeValue(i));
      }
    }
    return histogram;
  }
  
  public static int sumValues(final IntSignal signal) {
    final int count = signal.getTimestampCount();
    if (count == 0) return 0;

    if (signal instanceof BinarySignal) {
      return count;
    }

    int sum = 0;
    for (int i = 0; i < count; i++) {
      sum += signal.getNativeValue(i);
    }

    return sum;
  }

  public static double densityMacroAverage(final IntSignal signal, final TimeFrame frame, final long resolutionMillis) {
    if (signal.getTimestampCount() == 0) return 0;
    return toHistogramByResolution(signal, frame, resolutionMillis).getMean();
  }

  public static <T extends Number> double densityMicroAverage(
    final IntSignal signal,
    final TimeFrame frameMillis,
    final long microFrameLengthMillis,
    final long resolutionMillis,
    final boolean skipZeros
  ) {
    final long frameStart = frameMillis.getStartTime();
    final long frameEnd = frameMillis.getEndTime();
    final long frameDuration = frameEnd - frameStart;

    final ArrayList<TimeFrame> frames = new ArrayList<TimeFrame>((int) (frameDuration / resolutionMillis));

    long start = 0;
    for (;start <= frameDuration - microFrameLengthMillis; start += resolutionMillis) {
      frames.add(new TimeFrame(frameStart + start, frameStart + start + microFrameLengthMillis));
    }
    if (!frames.isEmpty() && frameEnd - frames.get(frames.size() - 1).getEndTime() > resolutionMillis) {
      frames.add(new TimeFrame(frameStart + start, frameEnd));
    }

    double total = 0;
    int count = 0;
    for (final TimeFrame frame : frames) {
      final double v = densityMacroAverage(signal, frame, resolutionMillis);
      if (v == 0 && skipZeros) continue;
      total += v;
      count++;
    }
    return total / count;
  }

  public static long lastTimestampBefore(final IntSignal signal, final long bound, final long defaultValue) {
    final TimeFrame frame = before(signal, bound).getTimeFrame();
    return frame.isEmpty() ? defaultValue : frame.getEndTime();
  }

  public static long lastTimestampBeforeFast(final IntSignal signal, final long bound, final long defaultValue) {
    if (signal.getTimestampCount() == 0) return defaultValue;
    final int floorIndex = signal.floorIndex(bound);
    if (floorIndex == -1) return defaultValue;
    final long timestamp = signal.getTimestamp(floorIndex);
    if (timestamp < bound) return timestamp;
    return floorIndex == 0 ? defaultValue : signal.getTimestamp(floorIndex - 1);
  }

  public static long firstTimestampAfter(final IntSignal signal, final long bound, final long defaultValue) {
    final TimeFrame frame = after(signal, bound).getTimeFrame();
    return frame.isEmpty() ? defaultValue : frame.getStartTime();
  }

  public static long firstTimestampAfterFast(final IntSignal signal, final long bound, final long defaultValue) {
    if (signal.getTimestampCount() == 0) return defaultValue;
    final int ceilIndex = signal.ceilIndex(bound);
    if (ceilIndex == -1) return defaultValue;
    final long timestamp = signal.getTimestamp(ceilIndex);
    if (timestamp > bound) return timestamp;
    return ceilIndex == signal.getTimestampCount() - 1 ? defaultValue : signal.getTimestamp(ceilIndex + 1);
  }

  public static int timestampCountInFrame(final IntSignal signal, final TimeFrame timeFrame) {
    if (signal.getTimestampCount() == 0) return 0;
    
    final long start = timeFrame.getStartTime();
    final long end = timeFrame.getEndTime();

    final int startIndex = signal.ceilIndex(start);
    if (startIndex == -1) return 0;

    final int endIndex = signal.floorIndex(end);
    if (endIndex == -1) return 0;

    return endIndex - startIndex + 1;
  }
}