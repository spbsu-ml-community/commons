package com.spbsu.net;

/**
 * User: lawless
 * Date: 02.07.2007
 */
public final class Policy {
  private final int maxConnection;
  private final int maxConnectionPerInterval;
  private final long intervalMs;

  public Policy(final int maxConnectionPerServer, final int maxConnectionPerInterval, final long intervalMs) {
    this.maxConnection = maxConnectionPerServer;
    this.maxConnectionPerInterval = maxConnectionPerInterval;
    this.intervalMs = intervalMs;
  }

  public int maxConnection() {
    return maxConnection;
  }

  public int maxConnectionPerInterval() {
    return maxConnectionPerInterval;
  }

  public long intervalMs() {
    return intervalMs;
  }
}
