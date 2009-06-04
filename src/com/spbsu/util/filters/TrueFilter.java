package com.spbsu.util.filters;

/**
 * Created by IntelliJ IDEA.
 * User: dunisher
 * Date: 07.06.2007
 * Time: 23:27:52
 * To change this template use File | Settings | File Templates.
 */
public class TrueFilter<T> implements Filter<T> {
  public boolean accept(T t) {
    return true;
  }

  public boolean accept(FilterVisitor visitor) {
    visitor.visit(this);
    return true;
  }
}