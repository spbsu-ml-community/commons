package com.spbsu.commons.text.lexical;

/**
 * User: terry
 * Date: 11.10.2009
 */
public class WordsTokenizer extends BaseTokenizer {

  private int offset;

  public WordsTokenizer(CharSequence text) {
    super(text);
  }

  protected CharSequence nextInner() {
    while (offset < text.length() && !Character.isLetterOrDigit(text.charAt(offset))) {
      offset++;
    }
    final int wordStart = offset;
    while (offset < text.length()
        && (text.charAt(offset) == '-'
        || text.charAt(offset) == '_'
        || Character.isLetterOrDigit(text.charAt(offset)))) {
      offset++;
    }
    if (wordStart == offset) {
      return null;
    } else {
      return text.subSequence(wordStart, offset);
    }
  }
}
