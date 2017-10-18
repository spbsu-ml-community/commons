package com.expleague.commons.util;

/**
 * User: Igor Kuralenok
 * Date: 24.07.2006
 * Time: 12:18:26
 */
public final class Pair<U, V> {
  public final U first;
  public final V second;

  public Pair(final U first, final V second) {
    this.first = first;
    this.second = second;
  }

  public U getFirst() {
    return first;
  }

  public V getSecond() {
    return second;
  }

  public static <U, V> Pair<U, V> create(final U u, final V v) {
    return new Pair<U, V>(u, v);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Pair pair = (Pair) o;

    return areEqual(first, pair.first) && areEqual(second, pair.second);
  }

  @Override
  public int hashCode() {
    int result;
    result = (first != null ? first.hashCode() : 0);
    result = 29 * result + (second != null ? second.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "(" + getFirst() + ", " + getSecond() + ")";
  }

  private static <T> boolean areEqual(final T t1, final T t2) {
    return t1 != null ? t1.equals(t2) : t2 == null;
  }
}
