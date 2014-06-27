package com.spbsu.commons.text.lexical;

/**
 * User: terry
 * Date: 11.10.2009
 */
public abstract class BaseTokenizer implements Tokenizer {

  protected final CharSequence text;
  private CharSequence nextToken;

  public BaseTokenizer(CharSequence text) {
    this.text = text;
  }

  @Override
  public boolean hasNext() {
    if (nextToken == null) {
      nextToken = nextInner();
    }
    return nextToken != null;
  }

  @Override
  public CharSequence next() {
    try {
      hasNext();
      return nextToken;
    } finally {
      nextToken = null;
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  protected abstract CharSequence nextInner();
}
