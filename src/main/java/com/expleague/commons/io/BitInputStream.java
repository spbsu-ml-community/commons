package com.expleague.commons.io;

import java.io.IOException;
import java.io.InputStream;


/**
 * User: solar
 * Date: 02.06.14
 * Time: 10:08
 */
public class BitInputStream implements BitInput {
  private static final int[] masks;
  static {
    masks = new int[9];
    for (int i = 0; i < masks.length; i++) {
      masks[i] = 0xFF >> (8 - i);
    }
  }

  private final InputStream stream;
  private int current = 0;
  private int offset = 8;

  public BitInputStream(final InputStream input) {
    stream = input;
  }

  @Override
  public int read(int count) {
    int result = next();
    final int bitsLeft = 8 - this.offset;
    result &= masks[bitsLeft];
    final int endOffset = bitsLeft - count;
    if (endOffset >= 0) {
      result >>>= endOffset;
      this.offset += count;
    }
    else {
      this.offset += bitsLeft;
      count -= bitsLeft;
      result <<= count;
      result |= read(count);
    }

    return result;
  }

  @Override
  public void mark() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void reset() {
    throw new UnsupportedOperationException();
  }

  private int next() {
    try {
      while (offset > 7) {
        offset -= 8;
        current = stream.read();
      }

      return current;
    }
    catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
