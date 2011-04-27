package com.spbsu.commons.util;

import java.util.*;

/**
 * User: alms
 * Date: 26.01.2009
 * Time: 20:44:55
 */
public class Factories {

  public static <T> List<T> arrayList(final int initalSize) {
    return new ArrayList<T>(initalSize);
  }

  public static <T> List<T> arrayList(final T... elements) {
    final List<T> result = new ArrayList<T>(elements.length);
    Collections.addAll(result, elements);

    return result;
  }

  public static <T> List<T> linkedList(final T... elements) {
    final List<T> result = new LinkedList<T>();
    Collections.addAll(result, elements);

    return result;
  }

  public static <K, V> HashMap<K, V> hashMap() {
    return new HashMap<K, V>();
  }

  public static <K, V> HashMap<K, V> hashMap(K key, V value) {
    HashMap<K, V> hashMap = new HashMap<K, V>();
    hashMap.put(key, value);
    return hashMap;
  }

  public static <K, V> TreeMap<K, V> treeMap() {
    return new TreeMap<K, V>();
  }

  public static <T> Set<T> hashSet(final T... elements) {
    return new HashSet<T>(Arrays.asList(elements));
  }

  public static <T> Set<T> hashSet(final Collection<T> elements) {
    return new HashSet<T>(elements);
  }

  public static <T> Set<T> linkedHashSet(final T... elements) {
    return new LinkedHashSet<T>(Arrays.asList(elements));
  }

  public static <T> Set<T> hashSet(int size, float loadFactor) {
    return new HashSet<T>(size, loadFactor);
  }

  public static <T> TreeSet<T> treeSet(final T... elements) {
    return new TreeSet<T>(Arrays.asList(elements));
  }

  private Factories() {
  }
}
