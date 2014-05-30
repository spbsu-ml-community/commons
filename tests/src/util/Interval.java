package util;

/**
 * User: Igor Kuralenok
 * Date: 31.08.2006
 */
public final class Interval {
  static long ourStartTime;
  static long beforeStart;

  public static void start() {
    Interval.ourStartTime = System.currentTimeMillis();
    beforeStart = 0;
  }

  public static void stopAndPrint() {
    System.out.println("Time (ms): " + (System.currentTimeMillis() - Interval.ourStartTime + beforeStart));
  }

  public static void stopAndPrint(String msg) {
    System.out.println(msg + "\nTime (ms): " + (System.currentTimeMillis() - Interval.ourStartTime + beforeStart));
  }

  public static void suspend() {
    beforeStart += (System.currentTimeMillis() - Interval.ourStartTime);
  }

  public static void resume() {
    ourStartTime = System.currentTimeMillis();
  }

  public static long time() {
    return (System.currentTimeMillis() - Interval.ourStartTime + beforeStart);
  }
}
