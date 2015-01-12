package com.spbsu.commons.io.codec;

import java.io.IOException;
import java.io.InputStream;


import com.spbsu.commons.io.codec.seq.ListDictionary;
import com.spbsu.commons.seq.ByteSeq;

/**
 * User: solar
 * Date: 28.08.14
 * Time: 12:35
 */
public class CSCInputStream extends InputStream {
  private final ArithmeticCoding.Decoder base;
  private final ListDictionary<Byte> dict;
  private final int EOF;
  private boolean EOFReached = false;

  public CSCInputStream(final InputStream base, final ListDictionary<Byte> dict, final int[] freqs) {
    EOF = freqs.length;
    final int[] freqsWithEOF = new int[freqs.length + 1];
    System.arraycopy(freqs, 0, freqsWithEOF, 0, freqs.length);
    this.base = new ArithmeticCoding.Decoder(base, freqsWithEOF);
    this.dict = dict;
  }

  private ByteSeq lastRead;
  private int offset;
  @Override
  public int read() throws IOException {
    if (lastRead == null || offset >= lastRead.length()) {
      final int read;
      if (EOFReached || (read = base.read()) == EOF) {
        EOFReached = true;
        return -1;
      }
      lastRead = (ByteSeq) dict.get(read);
      offset = 0;
    }
    return lastRead.byteAt(offset++);
  }
}
