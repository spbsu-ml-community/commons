package com.spbsu.text;

import com.spbsu.util.TextUtil;

/**
 * User: Igor Kuralenok
 * Date: 22.08.2006
 */
public class WordNormalizer {
  public static CharSequence canonicForm(CharSequence word) {
    return TextUtil.trim(TextUtil.toLowerCase(word));
  }
}
