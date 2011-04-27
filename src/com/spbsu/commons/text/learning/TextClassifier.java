package com.spbsu.commons.text.learning;

import java.util.Collection;
import java.util.Map;

/**
 * User: terry
 * Date: 04.10.2009
 */
public interface TextClassifier<T> {
  T classify(CharSequence charSequence);

  void learn(Map<T, Collection<CharSequence>> dataSet); 
}
