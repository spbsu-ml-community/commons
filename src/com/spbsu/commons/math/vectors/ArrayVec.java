package com.spbsu.commons.math.vectors;

/**
 * User: solar
 * Date: 16.01.2010
 * Time: 16:25:41
 */
public class ArrayVec implements Vec {
  final double[] values;
  private final IntBasis basis;

  public ArrayVec(double... values) {
    this.values = values;
    basis = new IntBasis(values.length);
  }

  @Override
  public double get(int i) {
    return values[i];
  }

  @Override
  public Vec set(int i, double val) {
    values[i] = val;
    return this;
  }

  @Override
  public Vec adjust(int i, double increment) {
    values[i] += increment;
    return this;
  }

  @Override
  public VecIterator nonZeroes() {
    return new VecIterator() {
      int index = -1;

      @Override
      public int index() {
        return index;
      }
      @Override
      public double value() {
        return values[index];
      }
      @Override
      public boolean isValid() {
        return index < values.length && index >= 0;
      }
      @Override
      public boolean advance() {
        while(++index < values.length && values[index] == 0);
        return isValid();
      }
      @Override
      public double setValue(double v) {
        return values[index] = v;
      }
    };
  }
 
  @Override
  public int nonZeroesCount() {
    int count = 0;
    for (double value : values) {
      if (value != 0)
        count++;
    }
    return count;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Vec && VecTools.equals(this, (Vec)o);
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    final VecIterator iter = nonZeroes();
    while (iter.advance()) {
      hashCode <<= 1;
      hashCode += iter.index();
      hashCode += iter.value() * 10000;
    }
    return hashCode;
  }
 
  @Override
  public Basis basis() {
    return basis;
  }
}
