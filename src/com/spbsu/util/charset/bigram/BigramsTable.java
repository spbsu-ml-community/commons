package com.spbsu.util.charset.bigram;

import java.util.*;

import org.jetbrains.annotations.NotNull;

/**
 * @author lyadzhin
 */
public class BigramsTable {
  public static final BigramsTable EMPTY_TABLE = new BigramsTable(Collections.<CharBigram,Double>emptyMap());

  public static BigramsTable create(@NotNull Map<CharBigram, Integer> bigram2Count, int totalCount) {
    final Map<CharBigram, Double> bigram2Frequency = new HashMap<CharBigram, Double>();
    for (Map.Entry<CharBigram, Integer> entry : bigram2Count.entrySet()) {
      final double frequency = (totalCount > 0) ? entry.getValue().doubleValue() / totalCount : 0;
      bigram2Frequency.put(entry.getKey(), frequency);
    }
    return new BigramsTable(bigram2Frequency);
  }

  public static BigramsTable create(@NotNull Properties properties) {
    final Map<CharBigram, Double> bigram2Frequency = new HashMap<CharBigram, Double>(properties.size());
    try {
      for (String property : properties.stringPropertyNames()) {
        final Double freq = Double.valueOf(properties.getProperty(property));
        bigram2Frequency.put(CharBigram.valueOf(property), freq);
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(e);
    }
    return new BigramsTable(bigram2Frequency);
  }

  private final Map<CharBigram, Double> bigram2Frequency;

  private BigramsTable(Map<CharBigram, Double> bigram2Frequency) {
    this.bigram2Frequency = bigram2Frequency;
  }

  public boolean containsBigram(@NotNull CharBigram b) {
    return bigram2Frequency.containsKey(b);
  }

  public Set<CharBigram> getBigrams() {
    return bigram2Frequency.keySet();
  }

  public double getFrequency(@NotNull CharBigram b) {
    return (bigram2Frequency.containsKey(b)) ? bigram2Frequency.get(b) : 0;
  }

  public Properties toProperties() {
    final Properties properties = new Properties();
    for (Map.Entry<CharBigram, Double> entry : bigram2Frequency.entrySet()) {
      properties.setProperty(entry.getKey().toString(), Double.toString(entry.getValue()));
    }
    return properties;
  }
}