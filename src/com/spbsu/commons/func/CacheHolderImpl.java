package com.spbsu.commons.func;

import java.util.HashMap;
import java.util.Map;

/**
 * User: solar
 * Date: 10/8/12
 * Time: 8:24 PM
 */
public class CacheHolderImpl {
  Map<Class<? extends Computable<? extends CacheHolderImpl, ?>>, ?> cache = new HashMap<Class<? extends Computable<? extends CacheHolderImpl, ?>>, Object>();

  public synchronized <CH extends CacheHolderImpl, R> R cache(Class<? extends Computable<CH, R>> type) {
    Object result = cache.get(type);
    if (result == null) {
      try {
        final Computable<CH, R> calculator = type.newInstance();
        //noinspection unchecked
        result = calculator.compute((CH)this);
      } catch (Exception e) {
        e.printStackTrace(); // I'd be extremely surprised if this happen :)
      }
    }
    //noinspection unchecked
    return (R)result;
  }

}
