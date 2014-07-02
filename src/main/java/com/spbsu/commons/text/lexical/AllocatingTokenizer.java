package com.spbsu.commons.text.lexical;

import com.spbsu.commons.seq.CharSeq;

/**
 * This is a wrapper for tokenizer that always allocates returned tokens
 * @author vp
 */
public class AllocatingTokenizer implements Tokenizer {
  private final Tokenizer tokenizer;

  public AllocatingTokenizer(final Tokenizer tokenizer) {
    this.tokenizer = tokenizer;
  }

  @Override
  public final boolean hasNext() {
    return tokenizer.hasNext();
  }

  /**
   * @return newly allocated instance of {@link CharSequence}
   */
  @Override
  public final CharSequence next() {
    final CharSequence token = tokenizer.next();
    return token != null ? allocate(token) : null;
  }

  @Override
  public final void remove() {
    throw new UnsupportedOperationException();
  }

  protected CharSequence allocate(final CharSequence source) {
    return CharSeq.allocateArrayBasedSequence(source);
  }
}
