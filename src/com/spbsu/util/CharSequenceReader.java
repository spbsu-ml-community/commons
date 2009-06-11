package com.spbsu.util;

import java.io.Reader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 09.06.2009
 * Time: 8:24:42
 * To change this template use File | Settings | File Templates.
 */
public class CharSequenceReader extends Reader {
  private int currentOffset = 0;
  private final CharSequence seq;

  public CharSequenceReader(CharSequence seq) {
    super(seq);
    this.seq = seq;
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    int read;
    for(read = -1; read < len && currentOffset < seq.length(); read++){
      cbuf[off + read + 1] = seq.charAt(currentOffset++);
    }
    return read >= 0 ? read + 1 : -1;
  }

  @Override
  public int read() throws IOException {
    return seq.charAt(currentOffset++);
  }

  @Override
  public void reset() throws IOException {
    currentOffset = 0;
  }

  public void close() throws IOException {
  }
}
