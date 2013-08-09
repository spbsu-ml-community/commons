package com.spbsu.commons.func.converters;


import com.spbsu.commons.func.Converter;
import com.spbsu.commons.util.Pair;

import java.nio.ByteBuffer;

/**
 * User: Igor Kuralenok
 * Date: 03.09.2006
 * Time: 20:19:01
 */
public class Pair2ByteBufferConverter<A, B> implements Converter<Pair<A, B>, ByteBuffer> {
  private final Converter<A, ByteBuffer> converterA;
  private final Converter<B, ByteBuffer> converterB;

  public Pair2ByteBufferConverter(Converter<A, ByteBuffer> converterA, Converter<B, ByteBuffer> converterB) {
    this.converterA = converterA;
    this.converterB = converterB;
  }

  @Override
  public Pair<A, B> convertFrom(ByteBuffer source) {
    return Pair.create(converterA.convertFrom(source), converterB.convertFrom(source));
  }

  @Override
  public ByteBuffer convertTo(Pair<A, B> object) {
    final ByteBuffer byteBufferA = converterA.convertTo(object.getFirst());
    final ByteBuffer byteBufferB = converterB.convertTo(object.getSecond());
    final ByteBuffer byteBuffer = ByteBuffer.allocate(byteBufferA.capacity() + byteBufferB.capacity()).put(byteBufferA).put(byteBufferB);
    byteBuffer.rewind();
    return byteBuffer;
  }
}
