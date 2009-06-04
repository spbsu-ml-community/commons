package com.spbsu.util.filters;

/**
 * Created by IntelliJ IDEA.
 * User: solar
 * Date: 02.06.2007
 * Time: 13:41:10
 * To change this template use File | Settings | File Templates.
 */
public interface Filter<T> {
  TrueFilter TRUE_FILTER = new TrueFilter();

  boolean accept(T t);
  boolean accept(FilterVisitor visitor);
}
