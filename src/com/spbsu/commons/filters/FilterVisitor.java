package com.spbsu.commons.filters;


/**
 * User: solar
 * Date: 02.06.2007
 * Time: 14:35:55
 */
public interface FilterVisitor {
  void visit(Filter filter);

  void visit(AndFilter filter);

  void visit(OrFilter filter);

  void visit(NotFilter filter);

  void visit(TrueFilter filter);
}
