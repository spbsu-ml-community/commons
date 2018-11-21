package com.expleague.commons.seq;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.BitSet;
import java.util.stream.IntStream;

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
    final CharSeqBuilder builder = new CharSeqBuilder();
    return chop(builder, delimiter) || builder.length() > 0 ? builder.build() : null;
  }

  public CharSeq chopQuiet(char delimiter) {
    try {
      return chop(delimiter);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public CharSeq chopQuiet(char... delimiters) {
    try {
      return chop(delimiters);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  public CharSeq chop(char... delimiters) throws IOException {
    final CharSeqBuilder builder = new CharSeqBuilder();
    chop(builder, mask(delimiters));
    return builder.length() == 0 ? null : builder.build();
  }

  @Nullable
  public CharSeq chop(BitSet mask) throws IOException {
    final CharSeqBuilder builder = new CharSeqBuilder();
    final int result = chop(builder, mask);
    if (result < 0 && builder.fragmentsCount() == 0)
      return null;
    return builder.build();
  }

  public int chop(CharSeqBuilder builder, char... delimiters) throws IOException {
    return chop(builder, mask(delimiters));
  }

  @NotNull
  public static BitSet mask(char... delimiters) {
    final BitSet mask = new BitSet(Character.MAX_VALUE);
    IntStream.range(0, delimiters.length).map(idx -> delimiters[idx]).forEach(mask::set);
    return mask;
  }

  public boolean chop(CharSeqBuilder builder, char delimiter) throws IOException {
    if (read < 0)
      return false;
    int start = offset;
    while (true) {
      if (offset >= read) {
        builder.append(buffer, start, read);
        readNext();
        if (read < 0)
          return false;
        start = 0;
      }
      if (buffer[offset++] == delimiter) {
        builder.append(buffer, start, offset - 1);
        return true;
      }
    }
  }

  public int chop(CharSeqBuilder builder, BitSet mask) throws IOException {
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
      if (mask.get(buffer[offset++])) {
        builder.append(buffer, start, offset - 1);
        return buffer[offset - 1];
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
      buffer = new char[64 * 1024];

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
