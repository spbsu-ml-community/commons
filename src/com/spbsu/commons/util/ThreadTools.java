package com.spbsu.commons.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

/**
 * User: solar
 * Date: 17.09.13
 * Time: 14:42
 */
public class ThreadTools {
  public static int COMPUTE_UNITS = Runtime.getRuntime().availableProcessors();
  public static ThreadPoolExecutor createBGExecutor(final String name, int queueSize) {
    final BlockingQueue<Runnable> workQueue;
    if (queueSize > 0) workQueue = new ArrayBlockingQueue<Runnable>(queueSize);
    else workQueue = new LinkedBlockingQueue<Runnable>();

    return new ThreadPoolExecutor(COMPUTE_UNITS, COMPUTE_UNITS, 5, TimeUnit.SECONDS, workQueue, new ThreadFactory() {
      @NotNull
      @Override
      public Thread newThread(Runnable r) {
        final Thread thread = new Thread(r, name);
        thread.setDaemon(true);
        return thread;
      }
    });

  }
}
