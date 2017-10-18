package com.expleague.commons.text.lexical;

import com.expleague.commons.seq.CharSeqTools;

/**
 * User: terry
 * Date: 12.12.2009
 */
public class LowerCaseTokenizer implements Tokenizer {
  private final Tokenizer tokenizer;

  public LowerCaseTokenizer(final Tokenizer tokenizer) {
    this.tokenizer = tokenizer;
  }

  @Override
  public boolean hasNext() {
    return tokenizer.hasNext();
  }

  @Override
  public CharSequence next() {
    final CharSequence token = tokenizer.next();
    return token != null ? CharSeqTools.toLowerCase(token) : null;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
