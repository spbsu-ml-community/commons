package com.spbsu.commons.func.converters.signals;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;
import com.spbsu.commons.math.signals.numeric.IntSignal;
import com.spbsu.commons.util.ArrayTools;
import com.spbsu.commons.util.logging.Logger;

import java.io.*;

/**
 * User: terry
 * Date: 15.12.2009
 */
public class IntSignal2BufferCompressingConverter implements Converter<IntSignal, Buffer> {
  private final static Logger LOG = Logger.create(IntSignal2BufferCompressingConverter.class);
  private static final long QUANT = 1000;

  @Override
  public IntSignal convertFrom(final Buffer _source) {
    final byte[] array = new byte[_source.limit()];
    _source.get(array);
    try {
      final DataInputStream input = new DataInputStream(new ByteArrayInputStream(array));
      final int count = input.readInt();
      if (count == 0) return new IntSignal(0);

      final long[] timestamps = new long[count];
      final int[] values = new int[count];
      long prev = input.readLong();
      for (int i = 0; i < count; i++) {
        final long timestamp = (long) input.readInt() * QUANT + prev;
        final int value = input.readInt();
        timestamps[i] = timestamp;
        values[i] = value;
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

    final ByteArrayOutputStream baos = new ByteArrayOutputStream(8 + 4 + count * 8);
    final DataOutputStream output = new DataOutputStream(baos);
    try {
      output.writeInt(count);
      long prev = signal.getTimestamp(0);
      output.writeLong(prev);
      for (int i = 0; i < count; i++) {
        final long timestamp = signal.getTimestamp(i);
        output.writeInt((int) ((timestamp - prev) / QUANT));
        output.writeInt(signal.getNativeValue(i));
        prev = timestamp;
      }
    } catch (IOException e) {
      LOG.error(e);
    }

    return BufferFactory.wrap(baos.toByteArray());
  }
}