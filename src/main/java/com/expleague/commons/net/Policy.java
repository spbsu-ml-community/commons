package com.expleague.commons.net;

/**
 * User: lawless
 * Date: 02.07.2007
 * Time: 15:32:13
 */
public class Policy {
  private final int maxConnection;
  private final int maxConnectionPerInterval;
  private final long intervalMs;

  public Policy() {
    this(1, 1, 1000);
  }

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

  @Override
  public String toString() {
    return "Policy[maxConnection=" + maxConnection + ", maxConnectionPerInterval=" + maxConnectionPerInterval
        + ", interval=" + intervalMs + "]";
  }
}
