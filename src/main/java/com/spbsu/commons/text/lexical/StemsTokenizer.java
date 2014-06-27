package com.spbsu.commons.text.lexical;

import com.spbsu.commons.text.stem.Stemmer;

/**
 * User: terry
 * Date: 05.12.2009
 */
public class StemsTokenizer extends WordsTokenizer {
  private final Stemmer stemmer;

  public StemsTokenizer(Stemmer stemmer, CharSequence text) {
    super(text);
    this.stemmer = stemmer;
  }

  @Override
  protected CharSequence nextInner() {
    final CharSequence next = super.nextInner();
    return next == null ? next : stemmer.stem(next);
  }
}
