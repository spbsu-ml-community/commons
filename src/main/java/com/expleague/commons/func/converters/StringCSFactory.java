package com.expleague.commons.func.converters;


import java.util.function.Function;

/**
 * User: solar
 * Date: 26.12.2009
 * Time: 18:39:55
 */
public class StringCSFactory implements Function<char[], String> {
  @Override
  public String apply(final char[] argument) {
    return new String(argument);
  }
}
