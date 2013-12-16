package com.spbsu.commons.util;

import com.spbsu.commons.math.vectors.Vec;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * User: solar
 * Date: 08.12.13
 * Time: 22:13
 */
public class ArrayPart<ArrayType> {
  public final ArrayType array;
  public final int start;
  public final int length;

  public ArrayPart(ArrayType array, int start, int length) {
    this.array = array;
    this.start = start;
    this.length = length;
  }

  public ArrayPart(ArrayType array) {
    this(array, 0, Array.getLength(array));
  }

  public ArrayPart<ArrayType> sub(int start, int length) {
    return new ArrayPart<ArrayType>(array, start + this.start, length);
  }

  public double doubleAt(int index) {
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
    throw new NotImplementedException();
  }
}
