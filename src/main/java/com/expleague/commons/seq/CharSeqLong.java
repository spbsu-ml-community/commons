package com.expleague.commons.seq;

public class CharSeqLong extends CharSeq {
  private final long content;

  private static final long[] tens =
      {1L, 1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L,
          100000000L, 1000000000L, 10000000000L, 100000000000L, 1000000000000L,
          10000000000000L, 100000000000000L, 1000000000000000L,10000000000000000L,
          100000000000000000L, 1000000000000000000L
      };

  public CharSeqLong(long content) {
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

    // Guessing 4 digit numbers will be more probable.
    // They are set in the first branch.
    if (n < 10000L) { // from 1 to 4
      if (n < 100L) { // 1 or 2
        if (n < 10L) {
          return 1;
        } else {
          return 2;
        }
      } else { // 3 or 4
        if (n < 1000L) {
          return 3;
        } else {
          return 4;
        }
      }
    } else  { // from 5 a 20 (albeit longs can't have more than 18 or 19)
      if (n < 1000000000000L) { // from 5 to 12
        if (n < 100000000L) { // from 5 to 8
          if (n < 1000000L) { // 5 or 6
            if (n < 100000L) {
              return 5;
            } else {
              return 6;
            }
          } else { // 7 u 8
            if (n < 10000000L) {
              return 7;
            } else {
              return 8;
            }
          }
        } else { // from 9 to 12
          if (n < 10000000000L) { // 9 or 10
            if (n < 1000000000L) {
              return 9;
            } else {
              return 10;
            }
          } else { // 11 or 12
            if (n < 100000000000L) {
              return 11;
            } else {
              return 12;
            }
          }
        }
      } else { // from 13 to ... (18 or 20)
        if (n < 10000000000000000L) { // from 13 to 16
          if (n < 100000000000000L) { // 13 or 14
            if (n < 10000000000000L) {
              return 13;
            } else {
              return 14;
            }
          } else { // 15 or 16
            if (n < 1000000000000000L) {
              return 15;
            } else {
              return 16;
            }
          }
        } else { // from 17 to ...¿20?
          if (n < 1000000000000000000L) { // 17 or 18
            if (n < 100000000000000000L) {
              return 17;
            } else {
              return 18;
            }
          } else { // 19? Can it be?
            // 10000000000000000000L is'nt a valid long.
            return 19;
          }
        }
      }
    }
  }
}
