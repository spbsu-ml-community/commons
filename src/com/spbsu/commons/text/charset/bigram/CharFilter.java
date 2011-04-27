package com.spbsu.commons.text.charset.bigram;

/**
 * @author lyadzhin
 */
public interface CharFilter {
  CharFilter ACCEPT_ALL_FILTER = new CharFilter() {
    public boolean accept(char c) {
      return true;
    }
  };

  CharFilter NOT_ASCII_FILTER = new CharFilter() {
    public boolean accept(char c) {
      return c > 128;
    }
  };

  boolean accept(char c);
}
