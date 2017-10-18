package com.expleague.commons.seq;

import java.io.IOException;
import java.io.Reader;

/**
 * User: solar
 * Date: 04.12.15
 * Time: 13:04
 */
public class CharSeqBuilderReader extends CharSeq {
  private static final int BUFFER_LENGTH = 4096 / 2;
  private final Reader reader;
  private final CharSeqBuilder builder = new CharSeqBuilder();
  private boolean closed = false;

  public CharSeqBuilderReader(Reader reader) {
    this.reader = reader;
  }

  @Override
  public char charAt(int offset) {
    readUpTo(offset);
    return builder.charAt(offset);
  }

  private void readUpTo(int offset) {
    while (!closed && offset >= builder.length()) {
      try {
        final char[] buffer = new char[BUFFER_LENGTH];
        if (!reader.ready())
          throw new DrainedOutException();
        final int read = reader.read(buffer);
        if (read == -1)
          closed = true;
        else {
          this.builder.append(buffer, 0, read);
        }
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }
  }

  @Override
  public CharSeq sub(int start, int end) {
    readUpTo(end);
    return builder.sub(start, end);
  }

  @Override
  public char[] toCharArray() {
    if (closed)
      return builder.toCharArray();
    throw new IllegalStateException();
  }

  @Override
  public void copyToArray(int start, char[] array, int offset, int length) {
    readUpTo(start + length);
    builder.copyToArray(start, array, offset, length);
  }

  @Override
  public CharSeq trim() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexOf(char ch) {
    return builder.indexOf(ch);
  }

  @Override
  public boolean isImmutable() {
    return true;
  }

  @Override
  public int length() {
    if (closed)
      return builder.length();
    throw new IllegalStateException();
  }

  public static class DrainedOutException extends RuntimeException {

  }
}
