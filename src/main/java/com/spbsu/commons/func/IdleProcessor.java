package com.spbsu.commons.func;

/**
 * User: terry
 * Date: 20.12.2009
 */
public class IdleProcessor<T> implements Processor<T> {
  private static final IdleProcessor INSTANCE = new IdleProcessor();

  @SuppressWarnings({"unchecked"})
  public static <T> IdleProcessor<T> create() {
    return INSTANCE;
  }

  @Override
  public void process(T arg) {
    //nohting
  }
}
