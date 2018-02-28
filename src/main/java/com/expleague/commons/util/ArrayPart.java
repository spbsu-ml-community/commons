package com.expleague.commons.util;

import java.lang.reflect.Array;

/**
 * User: solar
 * Date: 08.12.13
 * Time: 22:13
 */
public class ArrayPart<ArrayType> {
  public final ArrayType array;
  public final int start;
  public final int length;

  public ArrayPart(final ArrayType array, final int start, final int length) {
    this.array = array;
    this.start = start;
    this.length = length;
  }

  public ArrayPart(final ArrayType array) {
    this(array, 0, Array.getLength(array));
  }

  public ArrayPart<ArrayType> sub(final int start, final int length) {
    return new ArrayPart<ArrayType>(array, start + this.start, length);
  }

  public double doubleAt(final int index) {
    return Array.getDouble(array, index + start);
  }

  public ArrayType toArray() {
    if (array.getClass() == double[].class) {
      final double[] result = new double[length];
      for (int i = 0; i < result.length; i++) {
        result[i] = doubleAt(i);
      }
      return (ArrayType)result;
    }
    throw new UnsupportedOperationException();
  }
}
