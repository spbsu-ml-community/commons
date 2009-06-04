package com.spbsu.text;

import com.spbsu.util.TextUtil;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 22.08.2006
 * Time: 16:46:20
 * To change this template use File | Settings | File Templates.
 */
public class WordNormalizer {
  public static CharSequence canonicForm(CharSequence word) {
    return TextUtil.trim(TextUtil.toLowerCase(word));
  }
}
