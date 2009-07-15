package com.spbsu.util;

/**
 * User: Igor Kuralenok
 * Date: 31.08.2006
 */
public interface Computable<A,R> {
  R compute(A argument);
}
