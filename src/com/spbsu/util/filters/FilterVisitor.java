package com.spbsu.util.filters;

/**
 * Created by IntelliJ IDEA.
 * User: solar
 * Date: 02.06.2007
 * Time: 14:35:55
 * To change this template use File | Settings | File Templates.
 */
public interface FilterVisitor {
  void visit(Filter filter);

  void visit(AndFilter filter);

  void visit(OrFilter filter);

  void visit(NotFilter filter);

  void visit(TrueFilter filter);
}
