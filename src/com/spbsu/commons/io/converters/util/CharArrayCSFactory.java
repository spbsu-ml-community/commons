package com.spbsu.commons.io.converters.util;

import com.spbsu.commons.func.Computable;
import com.spbsu.commons.text.CharArrayCharSequence;

/**
 * User: solar
 * Date: 26.12.2009
 * Time: 18:39:55
 */
public class CharArrayCSFactory implements Computable<char[], CharArrayCharSequence> {
  public CharArrayCharSequence compute(char[] argument) {
    return new CharArrayCharSequence(argument, 0, argument.length);
  }
}