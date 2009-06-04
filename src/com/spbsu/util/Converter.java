package com.spbsu.util;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 17.08.2006
 * Time: 15:09:41
 * To change this template use File | Settings | File Templates.
 */
public interface Converter<T, S> {
  T convertTo(S source);
  S convertFrom(T object);
}
