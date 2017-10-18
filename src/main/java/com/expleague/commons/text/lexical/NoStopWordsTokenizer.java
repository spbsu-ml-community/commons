package com.expleague.commons.text.lexical;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a wrapper for tokenizer that never returns stop-words
 * @author vp
 */
public class NoStopWordsTokenizer extends BaseTokenizer {
  private final Tokenizer tokenizer;
  private final Set<CharSequence> stopWords;

  public NoStopWordsTokenizer(final Tokenizer tokenizer, final Set<CharSequence> dictionary) {
    super(null);
    this.tokenizer = tokenizer;
    this.stopWords = new HashSet<CharSequence>(dictionary);
  }

  @Override
  protected CharSequence nextInner() {
    while (tokenizer.hasNext()) {
      final CharSequence token = tokenizer.next();
      if (!stopWords.contains(token)) return token;
    }
    return null;
  }
}