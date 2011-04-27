package com.spbsu.commons.text.stem;

/**
 * User: selivanov
 * Date: 17.02.2010 : 23:34:38
 */
public interface StemmerElimination {
  public boolean isStemmerElimination(CharSequence word);
  CharSequence stem(CharSequence word);
}
