package com.spbsu.util.charset.bigram;

import com.spbsu.util.Logger;
import com.spbsu.util.charset.TextDecoder;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author lyadzhin
 */
public class BigramsTextDecoder implements TextDecoder {
  private static final Logger log = Logger.create(BigramsTextDecoder.class);

  // Tuning params. Obtained experimentally

  private static final double NO_BIGRAMS_PENALTY = Double.MAX_VALUE;
  private static final double UNKNOWN_BIGRAM_PENALTY = 15;
  private static final double LACK_OF_BIGRAMS_LIMIT = 0.2;
  private static final double LACK_OF_BIGRAMS_PENALTY = 15;

  private final BigramsTable baseBigramsTable;
  private final Set<Charset> availableCharsets;
  private BigramsTextAnalyzer textAnalyzer = new BigramsTextAnalyzer().setCharFilter(CharFilter.NOT_ASCII_FILTER);

  public BigramsTextDecoder(@NotNull BigramsTable baseBigramsTable, @NotNull Set<Charset> availableCharsets) {
    if (availableCharsets.size() < 2) {
      throw new IllegalArgumentException("must have at least two available charsets");
    }
    this.baseBigramsTable = baseBigramsTable;
    this.availableCharsets = availableCharsets;
  }

  public void setTextAnalyzer(@NotNull BigramsTextAnalyzer textAnalyzer) {
    this.textAnalyzer = textAnalyzer;
  }

  public CharSequence decodeText(@NotNull byte[] bytes) {
    if (bytes.length == 0) {
      return "";
    }
    final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    final TreeMap<Double, Charset> delta2Charset = new TreeMap<Double, Charset>();
    final HashMap<Charset, CharSequence> charset2Text = new HashMap<Charset, CharSequence>();
    for (Charset charset : availableCharsets) {
      final CharBuffer text = charset.decode(byteBuffer);
      byteBuffer.rewind();
      charset2Text.put(charset, text.toString()); //todo: string?
      final BigramsTable textBigramsTable = textAnalyzer.buildBigramsTable(text);
      final double delta = getBigramTablesDelta(baseBigramsTable, textBigramsTable, text.length());
//      log.info("charset: " + charset + ", delta: " + delta);
      delta2Charset.put(delta, charset);
    }
    final Charset mostProbableCharset = delta2Charset.firstEntry().getValue();
    return charset2Text.get(mostProbableCharset);
  }

  private double getBigramTablesDelta(BigramsTable base, BigramsTable another, int textLength) {
    final Set<CharBigram> bigrams = another.getBigrams();
    if (bigrams.size() == 0) {
      return NO_BIGRAMS_PENALTY;
    }
    double delta = 0;
    int totalBigramsFound = 0;
    for (final CharBigram b : bigrams) {
      if (base.containsBigram(b)) {
        delta += Math.abs(base.getFrequency(b) - another.getFrequency(b));
        totalBigramsFound++;
      } else {
        delta += UNKNOWN_BIGRAM_PENALTY;
      }
    }
    if (totalBigramsFound == 0) {
      return NO_BIGRAMS_PENALTY;
    }
    if (((double) bigrams.size() / textLength) < LACK_OF_BIGRAMS_LIMIT) {
      delta += LACK_OF_BIGRAMS_PENALTY * textLength;
    }
    return delta;
  }
}
