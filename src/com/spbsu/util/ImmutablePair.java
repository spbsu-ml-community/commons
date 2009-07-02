package com.spbsu.util;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 02.07.2009
 * Time: 10:30:48
 * To change this template use File | Settings | File Templates.
 */
public class ImmutablePair<U, V> extends Pair<U, V> {
  private int hash = 0;

  public ImmutablePair(final U first, final V second) {
    super(first, second);
  }

  @Override
  public boolean equals(Object o) {
    if (hash != 0 && o.getClass() == ImmutablePair.class) {
      final ImmutablePair pair = (ImmutablePair) o;
      if (pair.hash != 0 && pair.hash != hash)
        return false;
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return hash != 0 ? hash : (hash = super.hashCode());
  }
}
