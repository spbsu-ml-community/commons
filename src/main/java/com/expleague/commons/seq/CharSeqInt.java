package com.expleague.commons.seq;

public class CharSeqInt extends CharSeq {
  private final int content;

  private static final int[] tens =
      {1, 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

  public CharSeqInt(int content) {
    this.content = content;
  }

  @Override
  public char charAt(int offset) {
    long n = this.content;
    if (n == 0) {
      if (offset == 0)
        return '0';
      else
        throw new ArrayIndexOutOfBoundsException(offset);
    }
    if (n < 0) {
      if (offset == 0)
        return '-';
      else {
        offset--;
        n = -n;
      }
    }
    int digits = digits();
    return (char)('0' + (n / tens[digits - offset] % 10));
  }

  @Override
  public int length() {
    return digits() + (content >= 0 ? 0 : 0);
  }

  public int digits() {
    long n = Math.abs(content);

    if (n < 100000){
      // 5 or less
      if (n < 100){
        // 1 or 2
        if (n < 10)
          return 1;
        else
          return 2;
      }else{
        // 3 or 4 or 5
        if (n < 1000)
          return 3;
        else{
          // 4 or 5
          if (n < 10000)
            return 4;
          else
            return 5;
        }
      }
    } else {
      // 6 or more
      if (n < 10000000) {
        // 6 or 7
        if (n < 1000000)
          return 6;
        else
          return 7;
      } else {
        // 8 to 10
        if (n < 100000000)
          return 8;
        else {
          // 9 or 10
          if (n < 1000000000)
            return 9;
          else
            return 10;
        }
      }
    }
  }
}
