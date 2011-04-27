package com.spbsu.commons.text.charset.bigram;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.HashMap;
import java.nio.CharBuffer;

/**
 * @author lyadzhin
 */
public class BigramsTextAnalyzer {
  public static final int LENGTH_UNLIMITED = -1;

  private CharFilter charFilter = CharFilter.NOT_ASCII_FILTER;
  private int maxAnalysisLength = LENGTH_UNLIMITED;
  private boolean ignoreCase = true;

  public BigramsTextAnalyzer setCharFilter(@NotNull CharFilter charFilter) {
    this.charFilter = charFilter;
    return this;
  }

  public BigramsTextAnalyzer setIgnoreCase(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
    return this;
  }

  public BigramsTextAnalyzer setMaxAnalysisLength(int maxAnalysisLength) {
    if (maxAnalysisLength < 2 && maxAnalysisLength != LENGTH_UNLIMITED) {
      throw new IllegalArgumentException("analyzeDepthLimit must be >= 2 or == DEPTH_UNLIMITED");
    }
    this.maxAnalysisLength = maxAnalysisLength;
    return this;
  }

  public BigramsTable buildBigramsTable(@NotNull CharSequence s) {
    return buildBigramsTable(CharBuffer.wrap(s));
  }

  //todo: text is large, analyzeDepthLimit is set to limited, first 'analyzeDepthLimit'-symbols aren't accepted
  //todo: by filter
  public BigramsTable buildBigramsTable(@NotNull CharBuffer charBuffer) {
    final int length = charBuffer.length();
    if (length < 2) {
      return BigramsTable.EMPTY_TABLE;
    }
    final Map<CharBigram, Integer> bigram2Count = new HashMap<CharBigram, Integer>();
    final int lengthBound = (maxAnalysisLength != LENGTH_UNLIMITED && length > maxAnalysisLength) ? maxAnalysisLength : length - 1;
    int totalCount = 0;
    charBuffer.rewind();
    for (int i = 0; i < lengthBound; i++) {
      final char c1 = getChar(charBuffer, i);
      final char c2 = getChar(charBuffer, i + 1);
      if (!charFilter.accept(c1)) {
        continue;
      }
      if (!charFilter.accept(c2)) {
        i++;
        continue;
      }
      final CharBigram bigram = CharBigram.valueOf(c1, c2);
      final Integer count = (bigram2Count.containsKey(bigram)) ? bigram2Count.get(bigram) + 1 : 1;
      bigram2Count.put(bigram, count);
      totalCount++;
    }
    return BigramsTable.create(bigram2Count, totalCount);
  }

  private char getChar(CharBuffer charBuffer, int i) {
    return (ignoreCase) ? Character.toUpperCase(charBuffer.get(i)) : charBuffer.get(i);
  }

  //private static final int INITIAL_BUFFER_CAPACITY = 512;

  /*public BigramsTextAnalyzer setText(@NotNull Reader reader) throws IOException {
    char[] buffer = new char[INITIAL_BUFFER_CAPACITY];
    int totalRead = 0;
    while (true) {
      final int read = reader.read(buffer, totalRead, buffer.length - totalRead);
      if (read <= 0) {
        break;
      }
      totalRead += read;
      if (totalRead == buffer.length) {
        final char[] newBuffer = new char[buffer.length << 2];
        System.arraycopy(buffer, 0, newBuffer, 0, totalRead);
        buffer = newBuffer;
      }
    }
    if (totalRead == buffer.length) {
      this.text = buffer;
    } else {
      final char[] text = new char[totalRead];
      System.arraycopy(buffer, 0, text, 0, totalRead);
      this.text = text;
    }
    return this;
  }*/
  
}