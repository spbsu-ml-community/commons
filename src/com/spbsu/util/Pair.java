package com.spbsu.util;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 24.07.2006
 * Time: 12:18:26
 * To change this template use File | Settings | File Templates.
 */
public class Pair<U, V> {
  private U first;
  private V second;

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

  public static <U,V> Pair<U,V> create(final U u, final V v) {
    return new Pair<U, V>(u, v);
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Pair pair = (Pair) o;

    if (first != null ? !first.equals(pair.first) : pair.first != null) return false;
    return !(second != null ? !second.equals(pair.second) : pair.second != null);
  }

  public int hashCode() {
    int result;
    result = (first != null ? first.hashCode() : 0);
    result = 29 * result + (second != null ? second.hashCode() : 0);
    return result;
  }

  public String toString() {
    return "(" + getFirst() + ", " + getSecond() + ")";
  }

  public void setSecond(final V v) {
    second = v;
  }

  public void setFirst(final U first) {
    this.first = first;
  }
}
