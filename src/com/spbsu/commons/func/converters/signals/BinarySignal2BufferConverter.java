package com.spbsu.commons.func.converters.signals;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.func.converters.LongArray2BufferConverter;
import com.spbsu.commons.math.signals.numeric.BinarySignal;

/**
 * @author vp
 */
public class BinarySignal2BufferConverter implements Converter<BinarySignal, Buffer> {
  private final LongArray2BufferConverter timestampsConverter;

  public BinarySignal2BufferConverter() {
    this.timestampsConverter = new LongArray2BufferConverter();
  }

  @Override
  public BinarySignal convertFrom(final Buffer source) {
    final long[] stamps = timestampsConverter.convertFrom(source);
    return new BinarySignal(stamps);
  }

  @Override
  public Buffer convertTo(final BinarySignal signal) {
    return timestampsConverter.convertTo(signal.getTimestamps());
  }
}
