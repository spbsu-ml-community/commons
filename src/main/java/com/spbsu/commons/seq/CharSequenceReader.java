package com.spbsu.commons.seq;

import java.io.IOException;
import java.io.Reader;

/**
 * User: solar
 * Date: 16.07.14
 * Time: 13:26
 */
public class CharSequenceReader extends Reader {
  private final CharSequence sequence;
  int offset = 0;

  public CharSequenceReader(final CharSequence sequence) {
    this.sequence = sequence;
  }

  @Override
  public int read(final char[] cbuf, int off, final int len) throws IOException {
    int count = 0;
    while(count < len && offset < sequence.length()) {
      cbuf[off++] = sequence.charAt(offset++);
      count++;
    }
    return count;
  }

  @Override
  public void close() {
  }
}
