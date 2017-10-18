package com.expleague.commons.seq;

/**
 * User: solar
 * Date: 16.07.14
 * Time: 12:36
 */
public class SeqTools {
  public static <T> int indexOf(Seq<T> alpha, T s) {
    for (int i = 0; i < alpha.length(); i++) {
      if (s.equals(alpha.at(i)))
        return i;
    }
    return -1;
  }

  public static <T> Object copy(T a) {
    if (a instanceof CharSeq)
      return new CharSeqArray(((CharSeq)a).toCharArray());

//    throw new UnsupportedOperationException();
    return a;
  }
}
