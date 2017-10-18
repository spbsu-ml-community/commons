package com.expleague.commons.math.vectors.impl.basis;

import com.expleague.commons.math.vectors.Basis;

/**
 * User: terry
 * Date: 16.01.2010
 */
public class IntBasis implements Basis {
  private final int size;

  public IntBasis(final int size) {
    this.size = size;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean equals(final Object o) {
    return this == o || !(o == null || getClass() != o.getClass()) && size == ((IntBasis) o).size;

  }

  @Override
  public int hashCode() {
    return size;
  }
}
