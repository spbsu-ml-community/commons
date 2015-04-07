package com.spbsu.commons.io;

import org.jetbrains.annotations.NotNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * User: amosov-f
 * Date: 20.03.15
 * Time: 19:28
 */
public final class ChunkedOutputStream extends FilterOutputStream {
  static final byte MAGIC = 0x1E;

  private static final int DEFAULT_CHUNK_SIZE = 2048;

  @NotNull
  private final byte[] buffer;
  
  private int offset = 0;

  public ChunkedOutputStream(@NotNull final OutputStream out) {
    this(out, DEFAULT_CHUNK_SIZE);
  }

  public ChunkedOutputStream(@NotNull final OutputStream out, final int chunkSize) {
    super(out);
    this.buffer = new byte[chunkSize];
  }

  @Override
  public void write(final int b) throws IOException {
    buffer[offset++] = (byte) b;
    if (offset == buffer.length) {
      flush();
    }
  }

  @Override
  public void write(@NotNull final byte[] b, final int off, final int len) throws IOException {
    int curOff = off;
    while (curOff < off + len) {
      final int curLen = Math.min(off + len - curOff, buffer.length - offset);
      System.arraycopy(b, curOff, buffer, offset, curLen);
      offset += curLen;
      curOff += curLen;
      if (offset == buffer.length) {
        flush();
      }
    }
  }

  @Override
  public void flush() throws IOException {
    if (offset != 0) {
      writeInt(offset);
      out.write(buffer, 0, offset);
      out.write(MAGIC);
      offset = 0;
    }
    super.flush();
  }

  public void endChunks() throws IOException {
    flush();
    writeInt(-1);
    super.flush();
  }

  @SuppressWarnings("MagicNumber")
  private void writeInt(final int v) throws IOException {
    out.write((v >>> 24) & 0xFF);
    out.write((v >>> 16) & 0xFF);
    out.write((v >>> 8) & 0xFF);
    out.write(v & 0xFF);
  }
}
