package com.spbsu.util.converters;

import com.spbsu.util.Converter;
import com.spbsu.util.Pair;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 03.09.2006
 * Time: 20:19:01
 * To change this template use File | Settings | File Templates.
 */
public class Pair2ByteBufferConverter<A, B> implements Converter<Pair<A, B>, ByteBuffer> {
  private final Converter<A, ByteBuffer> converterA;
  private final Converter<B, ByteBuffer> converterB;

  public Pair2ByteBufferConverter(Converter<A, ByteBuffer> converterA, Converter<B, ByteBuffer> converterB) {
    this.converterA = converterA;
    this.converterB = converterB;
  }

  public Pair<A, B> convertTo(ByteBuffer source) {
    return Pair.create(converterA.convertTo(source), converterB.convertTo(source));
  }

  public ByteBuffer convertFrom(Pair<A, B> object) {
    final ByteBuffer byteBufferA = converterA.convertFrom(object.getFirst());
    final ByteBuffer byteBufferB = converterB.convertFrom(object.getSecond());
    final ByteBuffer byteBuffer = ByteBuffer.allocate(byteBufferA.capacity() + byteBufferB.capacity()).put(byteBufferA).put(byteBufferB);
    byteBuffer.rewind();
    return byteBuffer;
  }
}
