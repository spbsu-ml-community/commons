package com.expleague.commons.text.lexical;

import com.expleague.commons.text.stem.Stemmer;

/**
 * User: terry
 * Date: 05.12.2009
 */
public class StemsTokenizer extends WordsTokenizer {
  private final Stemmer stemmer;

  public StemsTokenizer(final Stemmer stemmer, final CharSequence text) {
    super(text);
    this.stemmer = stemmer;
  }

  @Override
  protected CharSequence nextInner() {
    final CharSequence next = super.nextInner();
    return next == null ? next : stemmer.stem(next);
  }
}
