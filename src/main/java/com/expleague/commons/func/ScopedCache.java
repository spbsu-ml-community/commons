package com.expleague.commons.func;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


import com.expleague.commons.util.Holder;
import com.expleague.commons.system.RuntimeUtils;

/**
 * User: solar
 * Date: 10/8/12
 * Time: 8:24 PM
 */
public class ScopedCache {
  private final CacheHolder owner;
  private final Map<Class<? extends CacheHolder>, Map<Class<? extends Function<? extends CacheHolder,?>>, Object>> cache = new HashMap<>();

  public ScopedCache(final Class<? extends CacheHolder> clazz, final CacheHolder owner) {
    this.owner = owner;
    RuntimeUtils.processSupers(clazz, arg -> {
      if (CacheHolder.class.isAssignableFrom(arg))
        //noinspection unchecked
        cache.put((Class<? extends CacheHolder>)arg, new HashMap<>());
      return false;
    });
  }

  public <CH extends CacheHolder, R> R cache(final Class<? extends Function<? super CH,R>> type, final Class<CH> scope) {
    final Holder<R> rHolder = new Holder<>();
    RuntimeUtils.processSupers(scope, arg -> {
      if (!CacheHolder.class.isAssignableFrom(arg))
        return false;
      //noinspection unchecked
      final R o = (R)cache.get(arg).get(type);
      rHolder.setValue(o);
      return o != null;
    });

    R result = rHolder.getValue();
    if (result == null) {
      try {
        final Function<? super CH, R> calculator = type.newInstance();
        //noinspection unchecked
        result = calculator.apply((CH)owner);
        //noinspection unchecked
        cache.get(scope).put((Class<? extends Function<? extends CacheHolder,?>>) type, result);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    //noinspection unchecked
    return (R)result;
  }

  public void copyScope(final ScopedCache to, final Class<? extends CacheHolder> scope) {
    for (final Map.Entry<Class<? extends CacheHolder>, Map<Class<? extends Function<? extends CacheHolder,?>>, Object>> entry : cache.entrySet()) {
      if (entry.getKey().isAssignableFrom(scope))
        to.cache.put(entry.getKey(), entry.getValue());
    }
  }
}
