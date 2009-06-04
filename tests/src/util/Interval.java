package util;

/**
 * Created by IntelliJ IDEA.
 * User: Igor Kuralenok
 * Date: 31.08.2006
 * Time: 16:36:19
 * To change this template use File | Settings | File Templates.
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
