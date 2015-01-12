package com.spbsu.commons.io;

import java.nio.ByteBuffer;

/**
 * User: solar
 * Date: 02.06.14
 * Time: 10:08
 */
public class BitInputBuffer implements BitInput {
  private static final int[] masks;
  static {
    masks = new int[9];
    for (int i = 0; i < masks.length; i++) {
      masks[i] = 0xFF >> (8 - i);
    }
  }

  private final ByteBuffer buffer;
  private int offset = 0;

  public BitInputBuffer(final ByteBuffer input) {
    buffer = input;
  }

  @Override
  public int read(int count) {
    int result = buffer.get(offset >> 3);
    final int bitsLeft = 8 - (this.offset & 7);
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

  int mark = 0;
  @Override
  public void mark() {
    mark = offset;
    buffer.mark();
  }

  @Override
  public void reset() {
    offset = mark;
    buffer.reset();
  }
}
