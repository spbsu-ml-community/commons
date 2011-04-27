package com.spbsu.commons.text.lexical;

import com.spbsu.commons.text.CharSequenceTools;

/**
 * User: terry
 * Date: 12.12.2009
 */
public class LowerCaseTokenizer implements Tokenizer {
  private final Tokenizer tokenizer;

  public LowerCaseTokenizer(Tokenizer tokenizer) {
    this.tokenizer = tokenizer;
  }

  @Override
  public boolean hasNext() {
    return tokenizer.hasNext();
  }

  @Override
  public CharSequence next() {
    final CharSequence token = tokenizer.next();
    return token != null ? CharSequenceTools.toLowerCase(token) : null;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
