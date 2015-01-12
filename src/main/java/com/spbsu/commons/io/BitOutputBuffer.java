package com.spbsu.commons.io;

import java.nio.ByteBuffer;

/**
 * User: solar
 * Date: 02.06.14
 * Time: 10:08
 */
public class BitOutputBuffer implements BitOutput {
  private static final int[] masks;
  static {
    masks = new int[9];
    for (int i = 0; i < masks.length; i++) {
      masks[i] = 0xFF >> (8 - i);
    }
  }

  private final ByteBuffer buffer;
  private int bitsLeft = 8;
  private byte lastByte;


  public BitOutputBuffer(final ByteBuffer output) {
    buffer = output;
  }

  @Override
  public void write(final int bits, int count) {
    final int shift = count - bitsLeft;
    lastByte |= (shift < 0 ? bits << -shift : bits >>> shift) & masks[bitsLeft];
    bitsLeft -= count;
    if (bitsLeft <= 0) { //underflow
      count = -bitsLeft;
      bitsLeft = 8;
      buffer.put(lastByte);
      lastByte = 0;
      if (count > 0) {
        write(bits, count);
      }
    }
  }

  @Override
  public void flush() {
    if (bitsLeft < 8)
      write(0, bitsLeft);
  }
}
