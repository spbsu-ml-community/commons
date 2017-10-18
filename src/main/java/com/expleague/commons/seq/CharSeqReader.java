package com.expleague.commons.seq;

import java.io.Reader;
import java.io.IOException;

/**
 * User: igorkuralenok
 * Date: 09.06.2009
 */
public class CharSeqReader extends Reader {
  private int currentOffset = 0;
  private final CharSequence seq;

  public CharSeqReader(final CharSequence seq) {
    super(seq);
    this.seq = seq;
  }

  @Override
  public int read(final char[] cbuf, final int off, final int len) throws IOException {
    final int read = Math.min(len, seq.length() - currentOffset);
    if (seq instanceof CharSeq) {
      ((CharSeq) seq).copyToArray(currentOffset, cbuf, off, read);
    }
    else if (seq instanceof String) {
      ((String) seq).getChars(currentOffset, currentOffset + read, cbuf, off);
    }
    else {
      for (int i = 0; i < read; i++) {
        cbuf[off + i] = seq.charAt(currentOffset+i);
      }
    }
    currentOffset += read;
    return read == 0 && currentOffset == seq.length() ? -1 : read;
  }

  @Override
  public int read() throws IOException {
    return seq.charAt(currentOffset++);
  }

  @Override
  public void reset() throws IOException {
    currentOffset = 0;
  }

  @Override
  public void close() throws IOException {
  }
}
