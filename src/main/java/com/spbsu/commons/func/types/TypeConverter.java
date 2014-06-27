package com.spbsu.commons.func.types;

/**
 * This class must have default constructor
 * User: solar
 * Date: 21.06.13
 * Time: 13:52
 */
public interface TypeConverter<F,T> {
  T convert(F from);
}
