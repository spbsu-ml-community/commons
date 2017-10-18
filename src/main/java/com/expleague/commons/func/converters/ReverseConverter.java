/**
 * 
 */
package com.expleague.commons.func.converters;


import com.expleague.commons.func.Converter;

/**
 * @author d1sharp
 *
 */
public class ReverseConverter<T, S> implements Converter<T, S> {

  private final Converter<S, T> delegate;

  public ReverseConverter(final Converter<S, T> delegate) {
    if (delegate == null) {
      throw new NullPointerException();
    }
    this.delegate = delegate;
  }
  
  @Override
  public T convertFrom(final S source) {
    return delegate.convertTo(source);
  }

  @Override
  public S convertTo(final T object) {
    return delegate.convertFrom(object);
  }
  
}
