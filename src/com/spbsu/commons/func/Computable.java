package com.spbsu.commons.func;

/**
 * User: Igor Kuralenok
 * Date: 31.08.2006
 * Time: 16:52:20
 */
public interface Computable<A,R> {
  R compute(A argument);
}
