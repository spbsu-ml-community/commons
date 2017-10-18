package com.expleague.commons.io.codec;

import com.expleague.commons.io.codec.seq.Dictionary;
import com.expleague.commons.seq.ByteSeq;
import com.expleague.commons.seq.ByteSeqBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

/**
 * User: solar
 * Date: 28.08.14
 * Time: 12:35
 */
public class CSCOutputStream extends OutputStream {
  private final ArithmeticCoding.Encoder base;
  private final Dictionary<Byte> dict;
  private final int EOF;

  public CSCOutputStream(final OutputStream base, final Dictionary<Byte> dict, final int[] freqs) {
    EOF = freqs.length;
    final int[] freqsWithEOF = new int[freqs.length + 1];
    System.arraycopy(freqs, 0, freqsWithEOF, 0, freqs.length);
    this.base = new ArithmeticCoding.Encoder(base, freqsWithEOF);
    this.dict = dict;
  }

  private final ByteSeqBuilder builder = new ByteSeqBuilder();
  @Override
  public void write(final int b) throws IOException {
    builder.add((byte)b);
  }

  @Override
  public void write(@NotNull final byte[] buf, final int off, final int len) throws IOException {
    builder.addAll(buf, off, len);
  }

  @Override
  public void flush() throws IOException {
    ByteSeq suffix = builder.build();
    while(suffix.length() > 0) {
      final int symbol = dict.search(suffix);
      suffix = suffix.sub(dict.get(symbol).length(), suffix.length());
      base.write(symbol);
    }
  }

  @Override
  public void close() throws IOException {
    base.write(EOF);
    base.flush();
    flush();
  }
}
