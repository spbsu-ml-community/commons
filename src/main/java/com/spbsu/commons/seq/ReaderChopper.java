package com.spbsu.commons.seq;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;

/**
 * User: solar
 * Date: 21.03.15
 * Time: 1:42
 */
public class ReaderChopper {
  private final Reader base;
  private char[] buffer = new char[4096];
  private int offset = 0;
  private int read = 0;

  public ReaderChopper(Reader base) {
    this.base = base;
  }

  @Nullable
  public CharSeq chop(char delimiter) throws IOException {
    if (read < 0)
      return null;
    final CharSeqBuilder builder = new CharSeqBuilder();
    int start = offset;
    while (true) {
      if (offset >= read) {
        builder.append(buffer, start, read);
        readNext();
        if (read < 0)
          return builder.length() > 0 ? builder.build() : null;
        start = 0;
      }
      if (buffer[offset++] == delimiter) {
        builder.append(buffer, start, offset - 1);
        return builder.build();
      }
    }
  }

  public CharSeq chopQuiet(char... delimiters) {
    try {
      return chop(delimiters);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final ThreadLocal<boolean[]> filter = new ThreadLocal<boolean[]>() {
    @Override
    protected boolean[] initialValue() {
      return new boolean[Character.MAX_VALUE];
    }
  };
  @Nullable
  public CharSeq chop(char... delimiters) throws IOException {
    final boolean[] filter = this.filter.get();
    for(int i = 0; i < delimiters.length; i++) {
      filter[delimiters[i]] = true;
    }
    try {
      if (read < 0)
        return null;
      final CharSeqBuilder builder = new CharSeqBuilder();
      int start = offset;
      while (true) {
        if (offset >= read) {
          builder.append(buffer, start, read);
          readNext();
          if (read < 0)
            return builder.length() > 0 ? builder.build() : null;
          start = 0;
        }
        if (filter[buffer[offset++]]) {
          builder.append(buffer, start, offset - 1);
          return builder.build();
        }
      }
    }
    finally {
      for(int i = 0; i < delimiters.length; i++) {
        filter[delimiters[i]] = false;
      }
    }
  }

  public void skip(int count) throws IOException {
    if (read < 0)
      return;
    while (count-- > 0) {
      readNext();
      offset++;
    }
  }

  public boolean eat(char ch) throws IOException {
    if (read < 0)
      return false;
    readNext();
    if (read > 0 && ch == buffer[offset]) {
      offset++;
      return true;
    }
    return false;
  }

  private void readNext() throws IOException {
    if (offset >= read) {
      //noinspection StatementWithEmptyBody
      while ((read = base.read(buffer)) == 0);
      offset = 0;
    }
  }
}
