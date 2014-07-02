package com.spbsu.commons.func;

import java.util.HashMap;
import java.util.Map;

/**
 * User: solar
 * Date: 10/8/12
 * Time: 8:24 PM
 */
public class CacheHolderImpl implements CacheHolder {
  Map<Class<? extends Computable<? extends CacheHolderImpl, ?>>, Object> cache = new HashMap<Class<? extends Computable<? extends CacheHolderImpl, ?>>, Object>();

  @Override
  public synchronized <CH extends CacheHolder, R> R cache(Class<? extends Computable<CH, R>> type) {
    Object result = cache.get(type);
    if (result == null) {
      try {
        final Computable<CH, R> calculator = type.newInstance();
        //noinspection unchecked
        result = calculator.compute((CH)this);
        //noinspection unchecked
        cache.put((Class<? extends Computable<? extends CacheHolderImpl, ?>>) type, result);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    //noinspection unchecked
    return (R)result;
  }
}
