package com.expleague.commons.math.vectors;

/**
 * User: solar
 * Date: 9/14/12
 * Time: 1:20 PM
 */
public interface Order {
  boolean advance();
  int index();
  int transIndex();
  boolean isValid();
}
