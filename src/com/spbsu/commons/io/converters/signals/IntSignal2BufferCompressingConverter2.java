package com.spbsu.commons.io.converters.signals;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;
import com.spbsu.commons.io.converters.NioConverterTools;
import com.spbsu.commons.math.signals.numeric.IntSignal;
import com.spbsu.commons.util.ArrayTools;
import com.spbsu.commons.util.logging.Logger;

import java.io.*;
import java.util.Arrays;

/**
 * User: terry
 * Date: 15.12.2009
 */
public class IntSignal2BufferCompressingConverter2 implements Converter<IntSignal, Buffer> {
  private final static Logger LOG = Logger.create(IntSignal2BufferCompressingConverter2.class);
  private static final int QUANT = 1000;

  @Override
  public IntSignal convertFrom(final Buffer _source) {
    final byte[] array = new byte[_source.limit()];
    _source.get(array);
    try {
      final DataInputStream input = new DataInputStream(new ByteArrayInputStream(array));
      final int count = NioConverterTools.restoreSize(input);

      if (count == 0) return new IntSignal();
      final long[] timestamps = new long[count];
      final int[] values = unpackValues(input);

      long prev = input.readLong();
      for (int i = 0; i < count; i++) {
        final long timestamp = (long) input.readInt() * QUANT + prev;
        timestamps[i] = timestamp;
        prev = timestamp;
      }

      return new IntSignal(timestamps, values);
    } catch (IOException e) {
      LOG.error(e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public Buffer convertTo(final IntSignal signal) {
    final int count = signal.getTimestampCount();
    if (count == 0) return BufferFactory.wrap(ArrayTools.EMPTY_BYTE_ARRAY);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final DataOutputStream output = new DataOutputStream(baos);
    try {
      NioConverterTools.storeSize(count, output);
      final int[] ints = signal.getNativeValues();
      packValues(ints, output);

      long prev = signal.getTimestamp(0);
      output.writeLong(prev);
      for (int i = 0; i < count; i++) {
        final long timestamp = signal.getTimestamp(i);
        output.writeInt((int) ((timestamp - prev) / QUANT));
        prev = timestamp;
      }
    } catch (IOException e) {
      LOG.error(e);
    }

    return BufferFactory.wrap(baos.toByteArray());
  }

  private static final int BIT_ONLY_UNITS = 1 << 7;
  private static final int BIT_SMALL_LENGTH = 1 << 6;
  private static final int BIT_SMALL_VALUES = 1 << 5;
  private static final int SMALL_LENGTH = 0xFF >> 3;

  public static void packValues(final int[] values, final DataOutput output) throws IOException {
    boolean onlyUnits = true;
    int max = Integer.MIN_VALUE;
    for (final int value : values) {
      if (value != 1) {
        onlyUnits = false;
        max = Math.max(max, value);
      }
    }

    final int maxCount = 0xFF >> 1;
    final boolean onlySmallValues = max <= maxCount;
    int flag = 0;
    if (onlySmallValues) flag |= BIT_SMALL_VALUES;
    if (onlyUnits) flag |= BIT_ONLY_UNITS;

    if (values.length < SMALL_LENGTH) {
      output.write(flag | BIT_SMALL_LENGTH | values.length);
    }
    else {
      output.write(flag);
      NioConverterTools.storeSize(values.length, output);
    }

    if (onlyUnits) return;

    if (onlySmallValues) {
      int unitAccum = 0;
      for (final int value : values) {
        if (value == 1) {
          if (++unitAccum == maxCount) {
            output.write(unitAccum);
            unitAccum = 0;
          }
        }
        else {
          if (unitAccum != 0) {
            output.write(unitAccum);
            unitAccum = 0;
          }
          output.write(value | 1 << 7);
        }
      }
      if (unitAccum != 0) output.write(unitAccum);
    }
    else {
      for (final int value : values) {
        NioConverterTools.storeSize(value, output);
      }
    }
  }

  public static int[] unpackValues(final DataInput input) throws IOException {
    final int flags = input.readUnsignedByte() & 0xFF;
    final int size;
    if ((flags & BIT_SMALL_LENGTH) != 0) {
      size = flags & (SMALL_LENGTH);
    }
    else size = NioConverterTools.restoreSize(input);
    final int[] result = new int[size];

    if ((flags & BIT_ONLY_UNITS) != 0) {
      Arrays.fill(result, 1);
      return result;
    }

    final boolean smallValues = (flags & BIT_SMALL_VALUES) != 0;
    if (smallValues) {
      int i = 0;
      while (i < size) {
        final int next = input.readUnsignedByte() & 0xFF;
        if ((next & 1 << 7) == 0) {
          Arrays.fill(result, i, i + next, 1);
          i += next;
        }
        else result[i++] = next & 0x7f;
      }
    }
    else {
      for (int i = 0; i < size; i++) {
        result[i] = NioConverterTools.restoreSize(input);
      }
    }
    return result;
  }
}