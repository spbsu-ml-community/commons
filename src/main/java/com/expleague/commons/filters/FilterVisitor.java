package com.expleague.commons.filters;


/**
 * User: solar
 * Date: 02.06.2007
 * Time: 14:35:55
 */
public interface FilterVisitor {
  boolean visit(Filter filter);

  boolean visit(AndFilter filter);

  boolean visit(OrFilter filter);

  boolean visit(NotFilter filter);

  boolean visit(TrueFilter filter);
}
