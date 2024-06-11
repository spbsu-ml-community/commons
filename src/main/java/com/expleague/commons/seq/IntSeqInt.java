package com.expleague.commons.seq;

import java.util.stream.IntStream;

/**
 * User: Igor Kuralenok
 * Date: 10.05.2006
 * Time: 17:55:23
 */
public class IntSeqInt extends IntSeq {
  public final int ch;

  public IntSeqInt(final int ch) {
    this.ch = ch;
  }

  @Override
  public int intAt(final int offset) {
    if (offset != 0)
      throw new ArrayIndexOutOfBoundsException();
    return ch;
  }

  @Override
  public Integer at(final int offset) {
      if (offset != 0)
          throw new ArrayIndexOutOfBoundsException();
    return ch;
  }


  @Override
  public IntSeq sub(final int start, final int end){
    if (end - start == 0)
      return EMPTY;
    if (end == 1 && start == 0)
      return this;
    throw new ArrayIndexOutOfBoundsException();
  }

  @Override
  public int length() {
    return 1;
  }

    @Override
    public int[] toArray() {
        return new int[]{ch};
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntSeq)) {
            return false;
        }

        return ((IntSeq) o).length() == 1 && ((IntSeq) o).intAt(0) == ch;
    }

    @Override
    public int hashCode() {
        return 31 + ch;
    }

    @Override
    public Class<Integer> elementType() {
        return int.class;
    }

    @Override
    public String toString() {
        return "[" + ch + ']';
    }

    @Override
    public IntStream stream() {
        return IntStream.of(ch);
    }
}
