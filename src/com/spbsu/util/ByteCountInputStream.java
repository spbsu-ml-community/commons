package com.spbsu.util;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Dan Dyugurov
 * Date: 08.08.2006
 * Time: 23:48:14
 * To change this template use File | Settings | File Templates.
 */
public class ByteCountInputStream extends FilterInputStream {

  long readBytes;

  public ByteCountInputStream(InputStream in) {
    super(in);
  }

  public long getReadBytes() {
    return readBytes;
  }

  public int read() throws IOException {
    final int b = super.read();
    if (b != -1) readBytes++;
    return b;
  }

  public int read(byte b[], int off, int len) throws IOException {
    final int length = super.read(b, off, len);
    if (length > 0) readBytes += length;
    return length;
  }
}
