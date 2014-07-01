package com.spbsu.commons.math.signals.numeric;

/**
 * @author vp
 */
public class QuantizedIntSignal extends IntSignal {
  private final long resolutionMs;

  public QuantizedIntSignal(final long resolutionMs) {
    this.resolutionMs = resolutionMs;
  }

  public QuantizedIntSignal(final long[] timestamps, final int[] values, final long resolutionMs) {
    super(quantize(timestamps, resolutionMs), values);
    this.resolutionMs = resolutionMs;
  }

  public QuantizedIntSignal(final int timestampCount, final long resolutionMs) {
    super(timestampCount);
    this.resolutionMs = resolutionMs;
  }

  public long getResolutionMs() {
    return resolutionMs;
  }

  @Override
  public void occur(final long timestamp, final Integer value) {
    super.occur(quantize(timestamp, resolutionMs), value);
  }

  public static long quantize(final long value, final long resolutionMs) {
    return resolutionMs * (value / resolutionMs);
  }

  public static long[] quantize(final long[] source, final long resolutionMs) {
    final long[] values = new long[source.length];
    for (int i = 0; i < values.length; i++) {
      values[i] = quantize(source[i], resolutionMs);
    }
    return values;
  }
}