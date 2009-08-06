package com.spbsu.net;

/**
 * User: vasiliy
 * Date: Aug 6, 2009
 */
public interface Policy {
  int maxConnection();
  int maxConnectionPerInterval();
  long intervalMs();
}
