package com.spbsu.commons.util.logging;

/**
 * User: Igor Kuralenok
 * Date: 31.08.2006
 */
public final class Interval {
  private static final ThreadLocal<Long> ourStartTime = new ThreadLocal<Long>();
  private static final ThreadLocal<Long> beforeStart = new ThreadLocal<Long>();

  public static void start() {
    ourStartTime.set(System.currentTimeMillis());
    beforeStart.set(0L);
  }

  public static void stopAndPrint() {
    stopAndPrint(null);
  }

  public static void stopAndPrint(final String message) {
    final String calledFrom;
    final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    if (stackTrace.length < 3) {
      calledFrom = "Called from empty space (stacktrace had less than 3 elements)";
    } else {
      calledFrom = stackTrace[2].toString();
    }
    if (message != null)
      System.out.println(message + ": " + (System.currentTimeMillis() - ourStartTime.get() + beforeStart.get()) + "(ms)");
    else
      System.out.println("Time (ms): " + (System.currentTimeMillis() - ourStartTime.get() + beforeStart.get())
              + "\t--- called from: " + calledFrom + (message != null ? " info:" + message : ""));
  }

  public static void suspend() {
    beforeStart.set(beforeStart.get() + System.currentTimeMillis() - ourStartTime.get());
  }

  public static void resume() {
    ourStartTime.set(System.currentTimeMillis());
  }

  public static long time() {
    return (System.currentTimeMillis() - ourStartTime.get() + beforeStart.get());
  }
}