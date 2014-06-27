/**
 * 
 */
package com.spbsu.commons.func.converters;


import com.spbsu.commons.func.Converter;

/**
 * @author d1sharp
 *
 */
public class ReverseConverter<T, S> implements Converter<T, S> {

  private final Converter<S, T> delegate;

  public ReverseConverter(Converter<S, T> delegate) {
    if (delegate == null) {
      throw new NullPointerException();
    }
    this.delegate = delegate;
  }
  
  @Override
  public T convertFrom(S source) {
    return delegate.convertTo(source);
  }

  @Override
  public S convertTo(T object) {
    return delegate.convertFrom(object);
  }
  
}
