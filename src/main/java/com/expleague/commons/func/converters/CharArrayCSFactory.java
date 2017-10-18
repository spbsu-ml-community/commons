package com.expleague.commons.func.converters;

import com.expleague.commons.func.Computable;
import com.expleague.commons.seq.CharSeqArray;

/**
 * User: solar
 * Date: 26.12.2009
 * Time: 18:39:55
 */
public class CharArrayCSFactory implements Computable<char[], CharSeqArray> {
  @Override
  public CharSeqArray compute(final char[] argument) {
    return new CharSeqArray(argument, 0, argument.length);
  }
}