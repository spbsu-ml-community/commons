package com.spbsu.commons.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: solar
 * Date: 17.09.13
 * Time: 14:42
 */
public class ThreadTools {
  public static int COMPUTE_UNITS = Runtime.getRuntime().availableProcessors();
  public static ThreadPoolExecutor createExecutor(final String name, int queueSize) {
    return new ThreadPoolExecutor(COMPUTE_UNITS, COMPUTE_UNITS, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueSize), new ThreadFactory() {
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
