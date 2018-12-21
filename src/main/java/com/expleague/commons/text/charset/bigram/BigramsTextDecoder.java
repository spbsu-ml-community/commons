package com.expleague.commons.text.charset.bigram;

import com.expleague.commons.text.charset.TextDecoder;
import com.expleague.commons.text.charset.TextDecoderTools;
import com.expleague.commons.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author lyadzhin
 */
public class BigramsTextDecoder implements TextDecoder {
  private static final Logger log = LoggerFactory.getLogger(BigramsTextDecoder.class);

  // Tuning params. Obtained experimentally

  private static final double NO_BIGRAMS_PENALTY = Double.MAX_VALUE;
  private static final double UNKNOWN_BIGRAM_PENALTY = 15;
  private static final double LACK_OF_BIGRAMS_LIMIT = 0.2;
  private static final double LACK_OF_BIGRAMS_PENALTY = 15;

  private final BigramsTable baseBigramsTable;
  private final List<Charset> availableCharsets;
  private BigramsTextAnalyzer textAnalyzer = new BigramsTextAnalyzer().setCharFilter(CharFilter.NOT_ASCII_FILTER);

  public BigramsTextDecoder(@NotNull final BigramsTable baseBigramsTable, @NotNull final List<Charset> availableCharsets) {
    if (availableCharsets.size() < 2) {
      throw new IllegalArgumentException("must have at least two available charsets");
    }
    this.baseBigramsTable = baseBigramsTable;
    this.availableCharsets = availableCharsets;
  }

  public void setTextAnalyzer(@NotNull final BigramsTextAnalyzer textAnalyzer) {
    this.textAnalyzer = textAnalyzer;
  }

  @Override
  public CharSequence decodeText(@NotNull final byte[] bytes) {
    if (bytes.length == 0) {
      return "";
    }
    final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    final TreeMap<Double, Charset> delta2Charset = new TreeMap<Double, Charset>();
    final HashMap<Charset, CharSequence> charset2Text = new HashMap<Charset, CharSequence>();
    for (final Charset charset : availableCharsets) {
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

  @Override
  public CharSequence decodeText(@NotNull final CharSequence input) {
    if (input.length() == 0) {
      return "";
    }

    // Analyzer fails to work on long sequences
    final CharSequence toAnalyze = input.length() < 200 ? input : input.subSequence(0, 200);

    final TreeMap<Double, Pair<Charset,Charset>> delta2Charset = new TreeMap<Double, Pair<Charset,Charset>>();

    {
      // At first, process input as is in UTF-8
      final byte[] utf8Bytes = TextDecoderTools.getBytes(toAnalyze, "UTF-8");

      final Charset charset = Charset.forName("UTF-8");

      final BigramsTable textBigramsTable = textAnalyzer.buildBigramsTable(toAnalyze);
      final double delta = getBigramTablesDelta(baseBigramsTable, textBigramsTable, toAnalyze.length());

      delta2Charset.put(delta, Pair.create(charset, charset));
    }

    for (final Charset textIn : availableCharsets) {
      final byte[] bytes = TextDecoderTools.getBytes(toAnalyze, textIn.name());
      final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

      for (final Charset textOut : availableCharsets) {
        if (textIn == textOut) continue;

        final CharBuffer text = textOut.decode(byteBuffer);
        byteBuffer.rewind();

        final BigramsTable textBigramsTable = textAnalyzer.buildBigramsTable(text);
        final double delta = getBigramTablesDelta(baseBigramsTable, textBigramsTable, text.length());
//      log.info("charset: " + charset + ", delta: " + delta);
        delta2Charset.put(delta, Pair.create(textIn, textOut));
      }
    }
    final Pair<Charset, Charset> mostProbable = delta2Charset.firstEntry().getValue();
    return mostProbable.getSecond().decode(ByteBuffer.wrap(TextDecoderTools.getBytes(input, mostProbable.getFirst().name())));
  }

  private double getBigramTablesDelta(final BigramsTable base, final BigramsTable another, final int textLength) {
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
