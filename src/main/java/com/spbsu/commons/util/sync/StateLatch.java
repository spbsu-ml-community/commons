package com.spbsu.commons.util.sync;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * User: solar
 * Date: 15.10.15
 * Time: 14:12
 */
public class StateLatch {
  public StateLatch(int initState) {
    state(initState);
  }

  public StateLatch() {
    state(1);
  }

  private static class Sync extends AbstractQueuedSynchronizer {
    public int state() { return super.getState(); }

    protected int tryAcquireShared(int mask) {
      final int state = getState();
      return (state & mask) != 0 ? 1 : -1;
    }

    protected boolean tryReleaseShared(int mask) {
      setState(mask);
      return true;
    }
  }

  private final Sync sync = new Sync();
  public int state() {
    return sync.state();
  }
  public void state(int state) {
    if (state <= 0 || Integer.bitCount(state) > 1)
      throw new IllegalArgumentException("State must be degree of two");
    sync.releaseShared(state);
  }
  public void advance() {
    state(state() << 1);
  }
  public void await(int state) throws InterruptedException {
    sync.acquireSharedInterruptibly(state);
  }

  public void state(int from, int to) {
    try {
      await(from);
      state(to);
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}