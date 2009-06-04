package com.spbsu.util;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 31.08.2006
 * Time: 16:52:20
 * To change this template use File | Settings | File Templates.
 */
public interface Computable<A,R> {
  R compute(A argument);
}
