package com.spbsu.commons.func;

/**
 * @author vp
 */
public interface TandemComputable<A,B,R> {
  R compute(A a, B b);
}