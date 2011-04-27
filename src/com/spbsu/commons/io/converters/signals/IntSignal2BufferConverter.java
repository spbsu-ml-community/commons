package com.spbsu.commons.io.converters.signals;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;
import com.spbsu.commons.io.converters.util.IntArray2BufferConverter;
import com.spbsu.commons.io.converters.util.LongArray2BufferConverter;
import com.spbsu.commons.math.signals.numeric.IntSignal;

/**
 * User: terry
 * Date: 15.12.2009
 */
public class IntSignal2BufferConverter implements Converter<IntSignal, Buffer> {
  private final LongArray2BufferConverter timestampsConverter;
  private final IntArray2BufferConverter intsConverter;

  public IntSignal2BufferConverter() {
    this.timestampsConverter = new LongArray2BufferConverter();
    this.intsConverter = new IntArray2BufferConverter();
  }

  @Override
  public IntSignal convertFrom(final Buffer source) {
    final long[] stamps = timestampsConverter.convertFrom(source);
    int[] ints = intsConverter.convertFrom(source);
    return new IntSignal(stamps, ints);
  }

  @Override
  public Buffer convertTo(final IntSignal signal) {
    final Buffer stamsBuffer = timestampsConverter.convertTo(signal.getTimestamps());
    final Buffer intsBuffer = intsConverter.convertTo(signal.getNativeValues());
    return BufferFactory.join(stamsBuffer, intsBuffer);
  }
}
