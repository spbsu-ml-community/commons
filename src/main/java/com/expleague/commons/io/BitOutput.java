package com.expleague.commons.io;

/**
 * User: solar
 * Date: 28.08.14
 * Time: 11:59
 */
public interface BitOutput {
  void write(int bits, int count);

  void flush();
}
