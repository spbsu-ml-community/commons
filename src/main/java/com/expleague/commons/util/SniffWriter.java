package com.expleague.commons.util;

import com.expleague.commons.seq.CharSeqBuilder;

import java.io.IOException;
import java.io.Writer;

/**
 * User: solar
 * Date: 09.12.14
 * Time: 14:11
 */
public class SniffWriter extends Writer {
  private final CharSeqBuilder builder = new CharSeqBuilder();
  private final Writer delegate;

  public SniffWriter(final Writer writer) {
    delegate = writer;
  }

  @Override
  public void write(final char[] cbuf, final int off, final int len) throws IOException {
    builder.append(cbuf, off, len);
    delegate.write(cbuf, off, len);
  }

  @Override
  public void flush() throws IOException {
    delegate.flush();
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }

  public CharSequence protocol() {
    return builder.build();
  }
}
