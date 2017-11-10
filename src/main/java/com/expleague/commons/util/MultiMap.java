package com.expleague.commons.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * User: alms
 * Date: 01.02.2009
 * Time: 1:53:41
 */
public class MultiMap<K, V> {
  public static Collection EMPTY = Collections.emptyList();

  public static <K, V> MultiMap<K, V> create() {
    return new MultiMap<K, V>();
  }

  private final Map<K, Collection<V>> mapa;

  public MultiMap() {
    this.mapa = new HashMap<>();
  }

  public void put(final K key, final V value) {
    mapa.computeIfAbsent(key, k -> new HashSet<>()).add(value);
  }

  @NotNull
  public Collection<V> get(final K key) {
    final Collection<V> v = mapa.get(key);

    if (v == null) {
      //noinspection unchecked
      return EMPTY;
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

  public Set<K> keySet() {
    return mapa.keySet();
  }

  public void putAll(K key, Collection<V> values) {
    mapa.computeIfAbsent(key, k -> new HashSet<>()).addAll(values);
  }
}
