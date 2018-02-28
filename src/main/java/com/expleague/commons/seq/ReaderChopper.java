package com.expleague.commons.seq;

import com.expleague.commons.util.ArrayTools;
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
  private char[] buffer = new char[0];
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

  private final ThreadLocal<boolean[]> filter = ThreadLocal.withInitial(() -> new boolean[Character.MAX_VALUE]);
  @Nullable
  public CharSeq chop(char... delimiters) throws IOException {
    final CharSeqBuilder builder = new CharSeqBuilder();
    final int result = chop(builder, delimiters);
    if (result < 0 && builder.fragmentsCount() == 0)
      return null;
    return builder.build();
  }

  public int chop(CharSeqBuilder builder, char... delimiters) throws IOException {
    final boolean[] filter = this.filter.get();
    for(int i = 0; i < delimiters.length; i++) {
      filter[delimiters[i]] = true;
    }
    try {
      if (read < 0)
        return -1;
      int start = offset;
      while (true) {
        if (offset >= read) {
          builder.append(buffer, start, read);
          readNext();
          if (read < 0)
            return -1;
          start = 0;
        }
        if (filter[buffer[offset++]]) {
          builder.append(buffer, start, offset - 1);
          return ArrayTools.indexOf(buffer[offset - 1], delimiters);
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
      buffer = new char[64 * 1024 * 1024];

      //noinspection StatementWithEmptyBody
      while ((read = base.read(buffer)) == 0);
      offset = 0;
    }
  }

  public CharSeq restQuiet() {
    try {
      return rest();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public CharSeq rest() throws IOException {
    if (read < 0)
      return CharSeq.EMPTY;
    final CharSeqBuilder builder = new CharSeqBuilder();
    builder.append(buffer, offset, read);
    offset = read;
    readNext();
    while (read >= 0) {
      builder.append(buffer, offset, read);
      offset = read;
      readNext();
    }
    return builder.build();
  }
}
