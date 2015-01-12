package com.spbsu.commons.func.types;

/**
 * User: solar
 * Date: 21.06.13
 * Time: 13:52
 */
public abstract class ShatteredTypeConverter<F,T> implements TypeConverter<F,T> {
  @Override
  public final T convert(final F from) {
    final T instance = createFrom(from);
    writeTo(from, instance);
    return instance;
  }

  protected abstract void writeTo(final F from, final T to);
  protected abstract T createFrom(final F from);
}
