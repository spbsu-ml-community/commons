package com.spbsu.commons.io.converters.util;

import com.spbsu.commons.func.Computable;

/**
 * User: solar
 * Date: 26.12.2009
 * Time: 18:39:55
 */
public class StringCSFactory implements Computable<char[], String> {
  public String compute(char[] argument) {
    return new String(argument);
  }
}
