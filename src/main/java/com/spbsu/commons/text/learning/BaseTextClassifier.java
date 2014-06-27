package com.spbsu.commons.text.learning;

import com.spbsu.commons.text.lexical.WordsTokenizer;
import com.spbsu.commons.func.Processor;

/**
 * User: terry
 * Date: 04.10.2009
 */
public abstract class BaseTextClassifier<T> implements TextClassifier<T> {

  protected void processText(CharSequence text, Processor<CharSequence> tremProcessor) {
    final WordsTokenizer tokenizer = new WordsTokenizer(text);
    while (tokenizer.hasNext()) {
      tremProcessor.process(tokenizer.next());
    }
  }
}
