package com.spbsu.commons.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * User: solar
 * Date: 02.06.14
 * Time: 10:08
 */
public class BitOutputStream implements BitOutput {
  private static final int[] masks;
  static {
    masks = new int[9];
    for (int i = 0; i < masks.length; i++) {
      masks[i] = 0xFF >> (8 - i);
    }
  }

  private final OutputStream buffer;
  private int bitsLeft = 8;
  private int lastByte;


  public BitOutputStream(final OutputStream output) {
    buffer = output;
  }

  @Override
  public void write(final int bits, int count) {
    try {
      final int shift = count - bitsLeft;
      lastByte |= (shift < 0 ? bits << -shift : bits >>> shift) & masks[bitsLeft];
      bitsLeft -= count;
      if (bitsLeft <= 0) { //underflow
        count = -bitsLeft;
        bitsLeft = 8;
        buffer.write(lastByte);
        lastByte = 0;
        if (count > 0) {
          write(bits, count);
        }
      }
    }
    catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  public void flush() {
    if (bitsLeft < 8)
      write(0, bitsLeft);
  }
}
