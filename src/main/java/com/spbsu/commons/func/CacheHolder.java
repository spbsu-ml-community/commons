package com.spbsu.commons.func;

/**
 * User: solar
 * Date: 10/8/12
 * Time: 8:24 PM
 */
public interface CacheHolder {
  public <CH extends CacheHolder, R> R cache(Class<? extends Computable<CH, R>> type);
}
