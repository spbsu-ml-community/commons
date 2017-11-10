package com.expleague.commons.func.converters;

import com.expleague.commons.seq.CharSeqArray;

import java.util.function.Function;

/**
 * User: solar
 * Date: 26.12.2009
 * Time: 18:39:55
 */
public class CharArrayCSFactory implements Function<char[], CharSeqArray> {
  @Override
  public CharSeqArray apply(final char[] argument) {
    return new CharSeqArray(argument, 0, argument.length);
  }
}