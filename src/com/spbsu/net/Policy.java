package com.spbsu.net;

/**
 * Created by IntelliJ IDEA.
 * User: lawless
 * Date: 02.07.2007
 * Time: 15:32:13
 * To change this template use File | Settings | File Templates.
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
