package com.spbsu.commons.func.types;

/**
 * User: solar
 * Date: 12.08.13
 * Time: 14:30
 */
public interface ConversionPack<F,T> {
  Class<? extends TypeConverter<F,T>> to();
  Class<? extends TypeConverter<T,F>> from();
}
