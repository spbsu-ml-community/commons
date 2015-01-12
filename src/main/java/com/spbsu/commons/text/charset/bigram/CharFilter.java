package com.spbsu.commons.text.charset.bigram;

/**
 * @author lyadzhin
 */
public interface CharFilter {
  CharFilter ACCEPT_ALL_FILTER = new CharFilter() {
    @Override
    public boolean accept(final char c) {
      return true;
    }
  };

  CharFilter NOT_ASCII_FILTER = new CharFilter() {
    @Override
    public boolean accept(final char c) {
      return c > 128;
    }
  };

  boolean accept(char c);
}
