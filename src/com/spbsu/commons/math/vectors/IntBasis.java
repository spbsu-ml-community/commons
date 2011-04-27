package com.spbsu.commons.math.vectors;

/**
 * User: terry
 * Date: 16.01.2010
 */
public class IntBasis implements Basis {
  private int size;

  public IntBasis(int size) {
    this.size = size;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || !(o == null || getClass() != o.getClass()) && size == ((IntBasis) o).size;

  }

  @Override
  public int hashCode() {
    return size;
  }
}
