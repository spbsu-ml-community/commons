package com.expleague.commons.io;

/**
 * User: solar
 * Date: 28.08.14
 * Time: 11:58
 */
public interface BitInput {
  int read(int count);

  void mark();

  void reset();
}
