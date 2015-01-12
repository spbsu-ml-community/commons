package com.spbsu.commons.func;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


import com.spbsu.commons.filters.Filter;
import com.spbsu.commons.system.RuntimeUtils;
import com.spbsu.commons.util.Holder;

/**
 * User: solar
 * Date: 10/8/12
 * Time: 8:24 PM
 */
public class ScopedCache {
  private final CacheHolder owner;
  Map<Class<? extends CacheHolder>, Map<Class<? extends Computable<? extends CacheHolder, ?>>, Object>> cache = new HashMap<>();

  public ScopedCache(final Class<? extends CacheHolder> clazz, final CacheHolder owner) {
    this.owner = owner;
    RuntimeUtils.processSupers(clazz, new Filter<Class<?>>() {
      @Override
      public boolean accept(final Class<?> arg) {
        if (CacheHolder.class.isAssignableFrom(arg))
          cache.put((Class<? extends CacheHolder>)arg, new HashMap<Class<? extends Computable<? extends CacheHolder, ?>>, Object>());
        return false;
      }
    });
  }

  public <CH extends CacheHolder, R> R cache(final Class<? extends Computable<? super CH, R>> type, final Class<CH> scope) {
    final Holder<R> rHolder = new Holder<>();
    RuntimeUtils.processSupers(scope, new Filter<Class<?>>() {
      public boolean accept(final Class<?> arg) {
        if (!CacheHolder.class.isAssignableFrom(arg))
          return false;
        final R o = (R)cache.get(arg).get(type);
        rHolder.setValue(o);
        return o != null;
      }
    });

    R result = rHolder.getValue();
    if (result == null) {
      try {
        final Computable<? super CH, R> calculator = type.newInstance();
        //noinspection unchecked
        result = calculator.compute((CH)owner);
        //noinspection unchecked
        cache.get(scope).put((Class<? extends Computable<? extends CacheHolder, ?>>) type, result);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    //noinspection unchecked
    return (R)result;
  }

  public void copyScope(final ScopedCache to, final Class<? extends CacheHolder> scope) {
    for (final Map.Entry<Class<? extends CacheHolder>, Map<Class<? extends Computable<? extends CacheHolder, ?>>, Object>> entry : cache.entrySet()) {
      if (entry.getKey().isAssignableFrom(scope))
        to.cache.put(entry.getKey(), entry.getValue());
    }
  }
}
