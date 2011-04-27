package com.spbsu.commons.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * User: alms
 * Date: 01.02.2009
 * Time: 1:53:41
 */
public class MultiMap<K, V> {

  public static <K, V> MultiMap<K, V> create() {
    return new MultiMap<K, V>();
  }

  private final Map<K, Collection<V>> mapa;

  public MultiMap() {
    this.mapa = Factories.hashMap();
  }

  public void put(final K key, final V value) {
    Collection<V> u = mapa.get(key);
    if (u == null) {
      u = Factories.hashSet();
      mapa.put(key, u);
    }
    u.add(value);
  }

  @NotNull
  public Collection<V> get(final K key) {
    final Collection<V> v = mapa.get(key);

    if (v == null) {
      return Collections.emptySet();
    }

    return Collections.unmodifiableCollection(v);
  }

  @NotNull
  public Set<K> getKeys() {
    return Collections.unmodifiableSet(mapa.keySet());
  }

  public boolean isEmpty() {
    return mapa.isEmpty();
  }
}
